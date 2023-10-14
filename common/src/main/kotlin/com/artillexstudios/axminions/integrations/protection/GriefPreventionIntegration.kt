package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Location
import org.bukkit.entity.Player

class GriefPreventionIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        return GriefPrevention.instance.allowBuild(player, location) == null
    }

    override fun register() {

    }
}