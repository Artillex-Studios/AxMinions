package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.cache.Caches
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent

class CacheListener : Listener {

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockFromToEvent(event: BlockFromToEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockBurnEvent(event: BlockBurnEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockExplodeEvent(event: BlockExplodeEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockFadeEvent(event: BlockFadeEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockFormEvent(event: BlockFormEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockGrowEvent(event: BlockGrowEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }

    @EventHandler
    fun onBlockPistonRetractEvent(event: BlockPistonRetractEvent) {
        val block = event.block
        Caches.get(block.world)?.update(block.x, block.y, block.z)
    }
}