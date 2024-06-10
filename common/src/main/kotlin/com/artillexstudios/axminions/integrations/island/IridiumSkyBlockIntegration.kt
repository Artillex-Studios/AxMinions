package com.artillexstudios.axminions.integrations.island

import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.integrations.types.IslandIntegration
import com.artillexstudios.axminions.api.utils.fastFor
import com.iridium.iridiumskyblock.IridiumSkyblock
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI
import org.bukkit.block.Block
import org.bukkit.entity.Player

class IridiumSkyBlockIntegration : IslandIntegration {

    override fun getIslandPlaced(player: Player): Int {
        val island = IridiumSkyblockAPI.getInstance().getUser(player).island
        if (island.isEmpty) {
            return 0
        }

        var amount = 0
        IridiumSkyblock.getInstance().teamManager.getTeamMembers(island.get()).fastFor {
            amount +=  AxMinionsAPI.INSTANCE.getDataHandler().getMinionAmount(it.uuid)
        }

        return amount
    }

    override fun handleBlockBreak(block: Block) {

    }

    override fun register() {

    }
}