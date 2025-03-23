package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.AxMinionsPlugin;
import com.artillexstudios.axminions.config.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class MinionSaver {
    private ScheduledFuture<?> future;

    public void start() {
        if (this.future != null) {
            LogUtils.error("Future was not cancelled, but it's loading!");
            return;
        }

        this.future = AsyncUtils.scheduleAtFixedRate(() -> {
            ObjectArrayList<Minion> copy = MinionWorldCache.copy();
            AxMinionsPlugin.instance().handler().saveMinions(copy).thenAccept(pair -> {
                if (Config.debug) {
                    LogUtils.debug("Saved {} minions in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
                }
            });
        }, 0, Config.autosaveSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.future != null && !this.future.isCancelled()) {
            this.future.cancel(false);
            this.future = null;

            ObjectArrayList<Minion> copy = MinionWorldCache.copy();
            AxMinionsPlugin.instance().handler().saveMinions(copy).toCompletableFuture().thenAccept(pair -> {
                if (Config.debug) {
                    LogUtils.debug("Saved {} minions in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
                }
            }).join();
        }
    }
}
