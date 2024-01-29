package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.cache.Caches
import com.artillexstudios.axminions.cache.ChunkCache
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

class WorldListener : Listener {

    @EventHandler
    fun onWorldLoadEvent(event: WorldLoadEvent) {
        AxMinionsPlugin.dataQueue.submit {
            MinionTypes.loadForWorld(event.world)
        }

        Caches.add(ChunkCache(event.world))
    }

    @EventHandler
    fun onWorldUnloadEvent(event: WorldUnloadEvent) {
        Caches.remove(event.world)
    }
}