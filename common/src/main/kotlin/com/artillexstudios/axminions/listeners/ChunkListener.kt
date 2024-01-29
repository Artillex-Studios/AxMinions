package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.cache.Caches
import com.artillexstudios.axminions.minions.Minions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class ChunkListener : Listener {

    @EventHandler
    fun onChunkLoadEvent(event: ChunkLoadEvent) {
        Minions.addTicking(event.chunk)
    }

    @EventHandler
    fun onChunkUnloadEvent(event: ChunkUnloadEvent) {
        val chunk = event.chunk
        Minions.removeTicking(chunk)

        Caches.get(event.world)?.invalidate(chunk.x, chunk.z)
    }
}