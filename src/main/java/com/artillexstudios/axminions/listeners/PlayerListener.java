package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axminions.database.DataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        DataHandler.updateUser(event.getPlayer()).thenAccept((result) -> {
            // TODO: Update user data
        });
    }
}
