package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public final class WorldListener implements Listener {

    @EventHandler
    public void onWorldLoadEvent(WorldLoadEvent event) {
        MinionArea area = MinionWorldCache.loadArea(event.getWorld());
        AxMinionsPlugin.instance().handler().loadMinions(event.getWorld()).toCompletableFuture()
                .thenAccept(loaded -> {
                    if (Config.debug) {
                        LogUtils.debug("Loaded {} minions in world {} in {} ms!", loaded.firstInt(), event.getWorld().getName(), loaded.secondLong() / 1_000_000);
                    }

                    if (area == null) {
                        return;
                    }

                    Scheduler.get().run(() -> {
                        if (Bukkit.getWorld(event.getWorld().getUID()) == null) {
                            return;
                        }

                        for (Chunk chunk : event.getWorld().getLoadedChunks()) {
                            area.startTicking(chunk);
                        }
                    });
                });
    }

    @EventHandler
    public void onWorldUnloadEvent(WorldUnloadEvent event) {
        MinionWorldCache.clear(event.getWorld());
        MinionWorldCache.remove(event.getWorld());
    }
}
