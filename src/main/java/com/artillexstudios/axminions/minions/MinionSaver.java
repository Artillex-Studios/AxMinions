package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.database.DataHandler;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class MinionSaver {
    private final ScheduledExecutorService service;
    private ScheduledFuture<?> future;

    public MinionSaver() {
        this.service = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "AxMinions-Autosave-Thread"));
    }

    public void start() {
        if (this.future != null) {
            LogUtils.error("Future was not cancelled, but it's loading!");
            return;
        }

        this.future = this.service.schedule(() -> {
            ObjectArrayList<Minion> copy = MinionWorldCache.copy();
            DataHandler.saveMinions(copy).thenAccept(pair -> {
                LogUtils.debug("Saved {} minions in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
            });
        }, Config.AUTOSAVE_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.future != null && !this.future.isCancelled()) {
            this.future.cancel(false);
            this.future = null;

            ObjectArrayList<Minion> copy = MinionWorldCache.copy();
            DataHandler.saveMinions(copy).toCompletableFuture().thenAccept(pair -> {
                LogUtils.debug("Saved {} minions in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
            }).join();
        }
    }
}
