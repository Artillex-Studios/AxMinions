package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.minions.Minions
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class SuperiorSkyBlock2Listener : Listener {
    private val ssbChunkFlags = IslandChunkFlags.ONLY_PROTECTED or IslandChunkFlags.NO_EMPTY_CHUNKS

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onIslandDisbandEvent(event: IslandDisbandEvent) {
        val minions = Minions.getMinions()

        Dimension.values().forEach { entry ->
            try {
                event.island.getAllChunksAsync(entry, ssbChunkFlags) { chunk ->
                    minions.forEach { minion ->
                        val ch = minion.getLocation().chunk
                        if (ch.x == chunk.x && ch.z == chunk.z && ch.world == chunk.world) {
                            minion.remove()
                            Bukkit.getPlayer(minion.getOwnerUUID())?.inventory?.addItem(minion.getAsItem())
                        }
                    }
                }
            } catch (_: NullPointerException) {
                // SuperiorSkyBlock api does it this way aswell
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onIslandKickEvent(event: IslandKickEvent) {
        val kicked = event.target.uniqueId
        val kickedPlayer = Bukkit.getPlayer(kicked)
        val minions = Minions.getMinions()

        Dimension.values().forEach { entry ->
            try {
                event.island.getAllChunksAsync(entry, ssbChunkFlags) { chunk ->
                    minions.forEach { minion ->
                        val ch = minion.getLocation().chunk
                        if (minion.getOwnerUUID() == kicked && ch.x == chunk.x && ch.z == chunk.z && ch.world == chunk.world) {
                            minion.remove()
                            kickedPlayer?.inventory?.addItem(minion.getAsItem())
                        }
                    }
                }
            } catch (_: NullPointerException) {
                // SuperiorSkyBlock api does it this way aswell
            }
        }
    }
}