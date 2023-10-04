package com.artillexstudios.axminions.commands

import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Range
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

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

    }
}