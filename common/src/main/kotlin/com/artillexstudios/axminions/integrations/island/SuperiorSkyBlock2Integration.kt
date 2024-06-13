package com.artillexstudios.axminions.integrations.island

import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.integrations.types.IslandIntegration
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import org.bukkit.block.Block
import org.bukkit.entity.Player

class SuperiorSkyBlock2Integration : IslandIntegration {

    override fun getIslandPlaced(player: Player): Int {
        var placed = 0
        SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.getIslandMembers(true)?.forEach {
            val a = AxMinionsAPI.INSTANCE.getDataHandler().getMinionAmount(it.uniqueId)
            placed += a
            if (Config.DEBUG()) {
                println("Member: ${it.name} - $a")
            }
        }

        if (Config.DEBUG()) {
            println("Placed total: $placed")
        }
        return placed
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