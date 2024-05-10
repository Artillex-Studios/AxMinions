package com.artillexstudios.axminions.integrations.island

import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.integrations.types.IslandIntegration
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import org.bukkit.entity.Player

class SuperiorSkyBlock2Integration : IslandIntegration {

    override fun getIslandPlaced(player: Player): Int {
        var placed = 0
        SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.getIslandMembers(true)?.forEach {
            placed += AxMinionsAPI.INSTANCE.getDataHandler().getMinionAmount(it.uniqueId)
        }

        return placed
    }

    override fun register() {

    }
}