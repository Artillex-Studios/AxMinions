package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import org.bukkit.Location
import org.bukkit.entity.Player

class IridiumSkyBlockIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
/*        val island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(location)

        return island.map { IridiumSkyblockAPI.getInstance().getUser(player) in it.members }.orElse(true)*/
        return true
    }

    override fun register() {

    }
}