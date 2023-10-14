package com.artillexstudios.axminions.integrations.economy

import com.artillexstudios.axminions.api.integrations.types.EconomyIntegration
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class VaultIntegration : EconomyIntegration {
    private lateinit var economy: Economy

    override fun getBalance(player: Player): Double {
        return economy.getBalance(player)
    }

    override fun giveBalance(player: Player, amount: Double) {
        economy.depositPlayer(player, amount)
    }

    override fun takeBalance(player: Player, amount: Double) {
        economy.withdrawPlayer(player, amount)
    }

    override fun register() {
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return

        economy = rsp.provider
    }
}