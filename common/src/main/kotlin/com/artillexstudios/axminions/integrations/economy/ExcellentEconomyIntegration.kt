package com.artillexstudios.axminions.integrations.economy

import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.integrations.types.EconomyIntegration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI

class ExcellentEconomyIntegration : EconomyIntegration {
    private lateinit var api: ExcellentEconomyAPI

    override fun getBalance(player: OfflinePlayer): Double {
        val online = player.player
        return if (online != null) {
            api.getBalance(online, Config.ECONOMY_CURRENCY())
        } else {
            api.getBalanceAsync(player.uniqueId, Config.ECONOMY_CURRENCY()).join()
        }
    }

    override fun giveBalance(player: OfflinePlayer, amount: Double) {
        val online = player.player
        if (online != null) {
            api.deposit(online, Config.ECONOMY_CURRENCY(), amount)
        } else {
            api.depositAsync(player.uniqueId, Config.ECONOMY_CURRENCY(), amount).join()
        }
    }

    override fun takeBalance(player: OfflinePlayer, amount: Double) {
        val online = player.player
        if (online != null) {
            api.withdraw(online, Config.ECONOMY_CURRENCY(), amount)
        } else {
            api.withdrawAsync(player.uniqueId, Config.ECONOMY_CURRENCY(), amount).join()
        }
    }

    override fun register() {
        val rsp = Bukkit.getServicesManager().getRegistration(ExcellentEconomyAPI::class.java) ?: return

        api = rsp.provider
    }
}
