package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public final class WorldListener implements Listener {

    @EventHandler
    public void onWorldLoadEvent(WorldLoadEvent event) {
        MinionWorldCache.loadArea(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloadEvent(WorldUnloadEvent event) {
        MinionArea area = MinionWorldCache.remove(event.getWorld());

        if (area != null) {
            // TODO: Remove all minions from the area
        }
    }
}
