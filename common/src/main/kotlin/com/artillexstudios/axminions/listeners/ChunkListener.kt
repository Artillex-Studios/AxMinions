package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.minions.Minions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class ChunkListener : Listener {

    @EventHandler
    fun onChunkLoadEvent(event: ChunkLoadEvent) {
        Minions.startTicking(event.chunk)
    }

    @EventHandler
    fun onChunkUnloadEvent(event: ChunkUnloadEvent) {
        Minions.stopTicking(event.chunk)
    }
}