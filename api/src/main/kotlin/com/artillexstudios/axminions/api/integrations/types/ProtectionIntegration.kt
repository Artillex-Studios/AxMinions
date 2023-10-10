package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.Location
import org.bukkit.entity.Player

interface ProtectionIntegration : Integration {

    fun canBuildAt(player: Player, location: Location): Boolean
}