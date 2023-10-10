package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.entity.Player

interface EconomyIntegration : Integration {

    fun getBalance(player: Player): Double

    fun giveBalance(player: Player, amount: Double)

    fun takeBalance(player: Player, amount: Double)
}