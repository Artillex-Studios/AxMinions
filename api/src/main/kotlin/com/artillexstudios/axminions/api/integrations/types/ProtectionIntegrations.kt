package com.artillexstudios.axminions.api.integrations.types

import org.bukkit.Location
import org.bukkit.entity.Player

interface ProtectionIntegrations {

    fun getProtectionIntegrations(): List<ProtectionIntegration>

    fun canBuildAt(player: Player, location: Location): Boolean
}