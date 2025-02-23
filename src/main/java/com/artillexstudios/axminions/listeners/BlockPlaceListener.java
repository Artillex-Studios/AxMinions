package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockPlaceListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Block block = event.getBlock();
        MinionArea area = MinionWorldCache.getArea(block.getWorld());
        if (area == null) {
            return;
        }

        Minion minion = area.getMinionAt(block.getLocation());
        if (minion == null) {
            return;
        }

        LogUtils.debug("Attempted to place block at minion!");
        event.setCancelled(true);
    }
}
