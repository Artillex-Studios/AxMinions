package com.artillexstudios.axminions.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import org.bukkit.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadUtils {
    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    public static void ensureMain(Location location, String message) {
        if (!Scheduler.get().isOwnedByCurrentRegion(location)) {
            log.error("Thread {} failed main thread check: {}", Thread.currentThread().getName(), message, new Throwable());
            throw new IllegalStateException(message);
        }
    }

    public static void ensureMain(String message) {
        if (!Scheduler.get().isGlobalTickThread()) {
            log.error("Thread {} failed main thread check: {}", Thread.currentThread().getName(), message, new Throwable());
            throw new IllegalStateException(message);
        }
    }

    public static void ensureMain(Location location) {
        ensureMain(location, "");
    }
}
