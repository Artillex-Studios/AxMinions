package com.artillexstudios.axminions.users;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.UUID;

public class Users {
    private static final AsyncLoadingCache<UUID, User> USER_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(3))
            .maximumSize(1000)
            .buildAsync(loader -> {
                return new User(null, null, null, 0, 0);
            });
}
