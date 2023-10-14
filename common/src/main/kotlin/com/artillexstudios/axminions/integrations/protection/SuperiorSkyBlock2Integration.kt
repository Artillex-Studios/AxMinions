package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import org.bukkit.Location
import org.bukkit.entity.Player

class SuperiorSkyBlock2Integration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        val localPlayer = SuperiorSkyblockAPI.getPlayer(player.uniqueId)

        val island = SuperiorSkyblockAPI.getIslandAt(location) ?: return true

        return island.isMember(localPlayer)
    }

    override fun register() {

    }
}