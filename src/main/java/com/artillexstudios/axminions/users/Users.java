package com.artillexstudios.axminions.users;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Users {
    private static final ConcurrentHashMap<UUID, User> USER_CACHE = new ConcurrentHashMap<>(100);

    public static void load(User user) {
        USER_CACHE.put(user.uuid(), user);
    }

    public static User get(Player player) {
        return USER_CACHE.get(player.getUniqueId());
    }
}
