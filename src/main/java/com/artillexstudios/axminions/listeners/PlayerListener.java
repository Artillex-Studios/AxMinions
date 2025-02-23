package com.artillexstudios.axminions.listeners;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.database.DataHandler;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.users.Users;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        AxMinionsPlugin.instance().handler().loadUser(event.getPlayer()).thenAccept(user -> {
            if (user == null) {
                LogUtils.warn("Failed to load user data for player {}!", event.getPlayer().getName());
                return;
            }

            LogUtils.debug("Loaded user for player: {}", event.getPlayer().getName());
            Users.load(user);
            
            ObjectArrayList<Minion> copy = MinionWorldCache.copy();
            for (Minion minion : copy) {
                if (minion.ownerId() == user.id()) {
                    user.minions().add(minion);
                    minion.extraData().put("owner_texture", user.texture());
                    minion.skin(minion.skin());
                }
            }
        });
    }
}
