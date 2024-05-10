package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.minions.Minions
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent
import org.bukkit.World.Environment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SuperiorSkyBlock2Listener : Listener {

    @EventHandler
    fun onIslandDisbandEvent(event: IslandDisbandEvent) {
        val minions = Minions.getMinions()

        Environment.entries.forEach { entry ->
            try {
                event.island.getAllChunksAsync(entry, true) { }.forEach { chunk ->
                    minions.forEach { minion ->
                        if (minion.getLocation().chunk == chunk) {
                            minion.remove()
                        }
                    }
                }
            } catch (_: NullPointerException) {
                // SuperiorSkyBlock api does it this way aswell
            }
        }
    }
}