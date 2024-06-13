package com.artillexstudios.axminions.integrations.island

import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.integrations.types.IslandIntegration
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import org.bukkit.Location
import org.bukkit.block.Block

class SuperiorSkyBlock2Integration : IslandIntegration {

    override fun getIslandAt(location: Location): String {
        return SuperiorSkyblockAPI.getIslandAt(location)?.uniqueId?.toString() ?: ""
    }

    override fun handleBlockBreak(block: Block) {
        val island = SuperiorSkyblockAPI.getIslandAt(block.location)

        if (island == null) {
            if (Config.DEBUG()) {
                println("Island is null at location: ${block.location}")
            }
        } else {
            island.handleBlockBreak(block)
        }
    }

    override fun register() {

    }
}