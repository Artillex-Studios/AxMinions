package com.artillexstudios.axminions.commands

import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.converter.LitMinionsConverter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("axminions", "minion", "minions")
class AxMinionsCommand {

    @Subcommand("give")
    @CommandPermission("axminions.command.give")
    @Description("Give a minion to a player")
    @AutoComplete("* @minionTypes * *")
    fun give(
        sender: CommandSender,
        receiver: Player,
        minionType: MinionType,
        @Default("1") level: Int,
        @Default("1") @Range(min = 1.0, max = 64.0) amount: Int
    ) {
        val item = minionType.getItem(level)
        item.amount = amount

        receiver.inventory.addItem(item)
    }

    @Subcommand("fuel give")
    @CommandPermission("axminions.command.fuel.give")
    fun give(sender: CommandSender, id: String, receiver: Player, @Default("1") amount: Int) {
        val item = ItemBuilder(Config.CHARGE_ITEMS().getSection(id)).get()
        item.amount = amount
        receiver.inventory.addItem(item)
    }

    @Subcommand("fuel add")
    @CommandPermission("axminions.command.fuel.add")
    fun add(player: Player, id: String, charge: Int) {
        val item = ItemBuilder(player.inventory.itemInMainHand).serialize(true)
        item["charge"] = charge
        AxMinionsPlugin.config.getConfig().set("charge.items.$id", item)
        AxMinionsPlugin.config.getConfig().save()
    }

    @Subcommand("reload")
    @CommandPermission("axminions.command.reload")
    @Description("Reload the configurations of the plugin")
    fun reload(sender: CommandSender) {
        val start = System.currentTimeMillis()
        AxMinionsPlugin.config.reload()
        AxMinionsPlugin.messages.reload()

        MinionTypes.getMinionTypes().fastFor { _, v ->
            v.getConfig().reload()
        }

        AxMinionsAPI.INSTANCE.getMinions().fastFor {
            it.markDirty()
        }

        AxMinionsPlugin.integrations.reload()

        sender.sendMessage(
            StringUtils.formatToString(
                Messages.PREFIX() + Messages.RELOAD_SUCCESS(),
                Placeholder.unparsed("time", (System.currentTimeMillis() - start).toString())
            )
        )
    }

    @Subcommand("convert")
    @CommandPermission("axminions.command.convert")
    @Description("Convert from a different plugin")
    fun convert(sender: CommandSender) {
        val converter = LitMinionsConverter()
        converter.convert()
    }

    @Subcommand("stats", "statistics")
    @CommandPermission("axminions.command.statistics")
    @Description("Get statistics of plugin")
    fun stats(sender: CommandSender) {
        val minions = AxMinionsAPI.INSTANCE.getMinions()
        var loaded = 0
        val total = minions.size

        minions.fastFor {
            if (it.isTicking()) {
                loaded++
            }
        }

        sender.sendMessage(
            StringUtils.formatToString(
                Messages.STATISTICS(),
                Placeholder.unparsed("ticking", loaded.toString()),
                Placeholder.unparsed("not-ticking", (total - loaded).toString()),
                Placeholder.unparsed("total", total.toString())
            )
        )
    }

    @Subcommand("reset")
    @CommandPermission("axminions.command.reset")
    fun reset(sender: CommandSender, offlinePlayer: OfflinePlayer) {
        val minions = AxMinionsAPI.INSTANCE.getMinions()
        val ownerUUID = offlinePlayer.uniqueId
        AxMinionsPlugin.dataQueue.submit {
            minions.fastFor {
                if (it.getOwnerUUID() == ownerUUID) {
                    it.remove()
                }
            }

            sender.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.RESET(), Placeholder.unparsed("player", offlinePlayer.name ?: "---")))
        }
    }

    @Subcommand("extraslot")
    @CommandPermission("axminions.command.extraslot")
    fun extraSlot(commandSender: CommandSender, offlinePlayer: OfflinePlayer, amount: Int) {
        AxMinionsPlugin.dataQueue.submit {
            val original = AxMinionsPlugin.dataHandler.getExtraSlots(offlinePlayer.uniqueId)
            AxMinionsPlugin.dataHandler.addUser(offlinePlayer.uniqueId, offlinePlayer.name ?: "---")
            AxMinionsPlugin.dataHandler.addExtraSlot(offlinePlayer.uniqueId, amount)
            commandSender.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SLOT_GIVE(), Placeholder.unparsed("player", offlinePlayer.name ?: "???"), Placeholder.unparsed("amount", amount.toString())))
            offlinePlayer.player?.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SLOT_RECEIVE(), Placeholder.unparsed("amount", amount.toString()), Placeholder.unparsed("from", original.toString()), Placeholder.unparsed("to", (original + amount).toString())))
        }
    }
}