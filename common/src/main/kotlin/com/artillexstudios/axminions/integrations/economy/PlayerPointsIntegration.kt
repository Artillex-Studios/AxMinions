package com.artillexstudios.axminions.integrations.economy

import com.artillexstudios.axminions.api.integrations.types.EconomyIntegration
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.OfflinePlayer

class PlayerPointsIntegration : EconomyIntegration {

    override fun getBalance(player: OfflinePlayer): Double {
        return PlayerPoints.getInstance().api.look(player.uniqueId).toDouble()
    }

    override fun giveBalance(player: OfflinePlayer, amount: Double) {
        PlayerPoints.getInstance().api.give(player.uniqueId, amount.toInt())
    }

    override fun takeBalance(player: OfflinePlayer, amount: Double) {
        PlayerPoints.getInstance().api.take(player.uniqueId, amount.toInt())
    }

    override fun register() {

    }
}