package com.artillexstudios.axminions.integrations.island

import com.artillexstudios.axminions.api.integrations.types.IslandIntegration
import org.bukkit.Location
import org.bukkit.block.Block
import world.bentobox.bentobox.BentoBox

class BentoBoxIntegration : IslandIntegration {

    override fun getIslandAt(location: Location): String {
        val island = BentoBox.getInstance().islandsManager.islandCache.getIslandAt(location)

        if (island !== null) {
            return island.uniqueId
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