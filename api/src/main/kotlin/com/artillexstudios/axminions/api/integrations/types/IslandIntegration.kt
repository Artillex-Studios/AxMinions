package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.block.Block
import org.bukkit.entity.Player

interface IslandIntegration : Integration {

    fun getIslandPlaced(player: Player): Int

    fun handleBlockBreak(block: Block)
}