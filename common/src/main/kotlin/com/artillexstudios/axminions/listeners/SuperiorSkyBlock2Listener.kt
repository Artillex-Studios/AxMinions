package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.minions.Minions
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent
import org.bukkit.Bukkit
import org.bukkit.World.Environment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.math.min

class SuperiorSkyBlock2Listener : Listener {

    @EventHandler
    fun onIslandDisbandEvent(event: IslandDisbandEvent) {
        val minions = Minions.getMinions()

        Environment.entries.forEach { entry ->
            try {
                event.island.getAllChunksAsync(entry, true) { chunk ->
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
                // ah ok if superior skyblock api does it this way then its fine 💀
            }
        }
    }

    @EventHandler
    fun onIslandKickEvent(event: IslandKickEvent) {
        val kicked = event.target.uniqueId
        val kickedPlayer = Bukkit.getPlayer(kicked)
        val minions = Minions.getMinions()

        Environment.entries.forEach { entry ->
            try {
                event.island.getAllChunksAsync(entry, true) { chunk ->
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