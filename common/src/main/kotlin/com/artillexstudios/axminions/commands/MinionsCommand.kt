package com.artillexstudios.axminions.commands

import com.artillexstudios.axminions.AxMinionsPlugin
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

/**
 * The commands for players, the ones for admins are in [AxMinionsCommand].
 */
@Command("minions", "minion")
class MinionsCommand {

    @Subcommand("list")
    @CommandPermission("axminions.command.list")
    @Description("Open the list of your minions")
    fun list(player: Player) {
        AxMinionsPlugin.minionList.open(player)
    }

    @Subcommand("shop")
    @CommandPermission("axminions.command.shop")
    @Description("Open the shop, where you can buy minions")
    fun shop(player: Player) {
        AxMinionsPlugin.shop.open(player)
    }

    @Subcommand("users")
    @CommandPermission("axminions.command.users")
    @Description("Open the list of the players who can use your minions")
    fun users(player: Player) {
        AxMinionsPlugin.users.open(player)
    }
}
