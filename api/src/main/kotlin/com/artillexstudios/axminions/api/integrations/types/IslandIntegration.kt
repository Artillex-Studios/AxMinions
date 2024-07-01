package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.Location
import org.bukkit.block.Block

interface IslandIntegration : Integration {

    fun getIslandAt(location: Location): String

    fun getExtra(location: Location): Int

    fun handleBlockBreak(block: Block)
}