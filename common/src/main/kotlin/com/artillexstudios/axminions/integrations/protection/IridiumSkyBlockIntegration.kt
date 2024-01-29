package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import com.iridium.iridiumskyblock.IridiumSkyblock
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI
import org.bukkit.Location
import org.bukkit.entity.Player

class IridiumSkyBlockIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        val island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(location)

        return island.map { IridiumSkyblockAPI.getInstance().getUser(player) in IridiumSkyblock.getInstance().teamManager.getMembersOnIsland(it) }.orElse(true) || IridiumSkyblockAPI.getInstance().getUser(player).isBypassing
    }

    override fun register() {

    }
}