package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public final class ChunkListener implements Listener {

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event) {
        MinionArea area = MinionWorldCache.getArea(event.getWorld());
        if (area != null) {
            area.startTicking(event.getChunk());
        }
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        MinionArea area = MinionWorldCache.getArea(event.getWorld());
        if (area != null) {
            area.stopTicking(event.getChunk());
        }
    }
}
