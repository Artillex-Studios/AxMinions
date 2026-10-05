package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.minions.Minions
import com.bgsoftware.superiorskyblock.api.island.Island
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class SuperiorSkyBlock2Listener : Listener {
    private val ssbChunkFlags = IslandChunkFlags.ONLY_PROTECTED or IslandChunkFlags.NO_EMPTY_CHUNKS

    private fun returnMinionsOnIsland(island: Island, predicate: (Minion) -> Boolean) {
        val minions = Minions.getMinions()

        Dimension.values().forEach { entry ->
            try {
                island.getAllChunksAsync(entry, ssbChunkFlags) { chunk ->
                    minions.forEach { minion ->
                        val ch = minion.getLocation().chunk
                        if (predicate(minion) && ch.x == chunk.x && ch.z == chunk.z && ch.world == chunk.world) {
                            val player = Bukkit.getPlayer(minion.getOwnerUUID())
                            val tool = minion.getTool()
                            val asItem = minion.getAsItem()
                            val location = minion.getLocation()
                            minion.remove()

                            val itemsToReturn = if (tool != null) arrayOf(tool, asItem) else arrayOf(asItem)

                            val leftover = if (Config.RETURN_MINIONS_ON_ISLAND_LEAVE()) {
                                player?.inventory?.addItem(*itemsToReturn)
                                    ?: itemsToReturn.withIndex().associateTo(HashMap()) { (i, item) -> i to item }
                            } else {
                                itemsToReturn.withIndex().associateTo(HashMap()) { (i, item) -> i to item }
                            }

                            leftover.forEach { (_, itemStack) ->
                                AxMinionsPlugin.integrations.getStackerIntegration().dropItemAt(itemStack, itemStack.amount, location)
                            }
                        }
                    }
                }
            } catch (_: NullPointerException) {
                // SuperiorSkyBlock api does it this way aswell
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onIslandDisbandEvent(event: IslandDisbandEvent) {
        returnMinionsOnIsland(event.island) { true }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onIslandKickEvent(event: IslandKickEvent) {
        val kicked = event.target.uniqueId
        returnMinionsOnIsland(event.island) { it.getOwnerUUID() == kicked }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onIslandQuitEvent(event: IslandQuitEvent) {
        val quitting = event.player.uniqueId
        returnMinionsOnIsland(event.island) { it.getOwnerUUID() == quitting }
    }
}