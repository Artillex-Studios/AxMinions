package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axminions.users.LinkData;
import com.artillexstudios.axminions.users.User;
import com.artillexstudios.axminions.users.Users;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class MinionLinkListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType().isAir()) return;
        User user = Users.get(event.getPlayer());
        if (user == null) return;
        LinkData linkData = user.linkData();
        if (linkData == null) return;
        // TODO: AdvancedChests integration, better checking
        if (!(clickedBlock.getState() instanceof Container)) {
            // TODO: Message saying couldn't link
            user.linkData(null);
            event.setCancelled(true);
            event.getPlayer().sendMessage("Failed to link");
            return;
        }

        linkData.minion().withLinkedChest(clickedBlock.getLocation());
        user.linkData(null);
        event.setCancelled(true);
        event.getPlayer().sendMessage("Linked");
    }
}
