package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

interface EconomyIntegration : Integration {

    fun getBalance(player: OfflinePlayer): Double

    fun giveBalance(player: OfflinePlayer, amount: Double)

    fun takeBalance(player: OfflinePlayer, amount: Double)
}