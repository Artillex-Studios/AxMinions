package com.artillexstudios.axminions.integrations.island

import com.artillexstudios.axminions.api.integrations.types.IslandIntegration
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI
import org.bukkit.Location
import org.bukkit.block.Block

class IridiumSkyBlockIntegration : IslandIntegration {

    override fun getIslandAt(location: Location): String {
        val island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(location)

        if (island.isPresent) {
            return island.get().id.toString()
        }

        return ""
    }

    override fun getExtra(location: Location): Int {
        return 0
    }


    override fun handleBlockBreak(block: Block) {

    }

    override fun register() {

    }
}