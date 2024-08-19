package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axminions.database.DataHandler;
import com.artillexstudios.axminions.users.User;
import com.artillexstudios.axminions.users.Users;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        DataHandler.loadUser(event.getPlayer()).thenAccept(user -> {
            LogUtils.debug("Loaded user");
            Users.load(user);
            // TODO: Update all minions placed by the user
        });
    }
}
