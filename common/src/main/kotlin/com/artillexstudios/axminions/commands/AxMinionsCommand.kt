package com.artillexstudios.axminions.commands

import com.artillexstudios.axapi.libs.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import com.artillexstudios.axapi.libs.lamp.annotation.AutoComplete
import com.artillexstudios.axapi.libs.lamp.annotation.Command
import com.artillexstudios.axapi.libs.lamp.annotation.Default
import com.artillexstudios.axapi.libs.lamp.annotation.Description
import com.artillexstudios.axapi.libs.lamp.annotation.Range
import com.artillexstudios.axapi.libs.lamp.annotation.Subcommand
import com.artillexstudios.axapi.libs.lamp.bukkit.annotation.CommandPermission
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.fastFor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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

    @Subcommand("reload")
    @CommandPermission("axminions.command.reload")
    @Description("Reload the configurations of the plugin")
    fun reload(sender: CommandSender) {
        val start = System.currentTimeMillis()
        AxMinionsPlugin.config.reload()
        AxMinionsPlugin.messages.reload()

        MinionTypes.getMinionTypes().forEach {
            it.value.getConfig().reload()
        }

        AxMinionsAPI.INSTANCE.getMinions().fastFor {
            it.markDirty()
        }

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

    }

    @Subcommand("stats", "statistics")
    @CommandPermission("axminions.command.statistics")
    @Description("Get statistics of plugin")
    fun stats(sender: CommandSender) {
        val minions = AxMinionsAPI.INSTANCE.getMinions()
        var loaded = 0
        val total = minions.size

        minions.fastFor {
            if (it.getType().isTicking(it)) {
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
}