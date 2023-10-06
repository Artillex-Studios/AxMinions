package com.artillexstudios.axminions.commands

import com.artillexstudios.axapi.libs.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import com.artillexstudios.axapi.libs.lamp.annotation.*
import com.artillexstudios.axapi.libs.lamp.bukkit.annotation.CommandPermission
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command("axminions", "minion", "minions")
class AxMinionsCommand {

    @Subcommand("give")
    @CommandPermission("axminions.command.give")
    @Description("Give a minion to a player")
    @AutoComplete("* @minionTypes * *")
    fun give(sender: CommandSender, @Default("me") receiver: Player, @Default("collector") minionType: MinionType, @Default("1") level: Int, @Default("1") @Range(min = 1.0, max = 64.0) amount: Int) {
        receiver.inventory.addItem(minionType.getItem())
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

        sender.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.RELOAD_SUCCESS(), Placeholder.unparsed("time", (System.currentTimeMillis() - start).toString())))
    }
}