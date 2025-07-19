package com.artillexstudios.axminions.minions.ticker;

import com.artillexstudios.axapi.scheduler.ScheduledTask;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.jfr.MinionTickEvent;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionTicker;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.utils.ChunkPos;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class FoliaMinionTicker implements MinionTicker {
    private ScheduledTask task;

    @Override
    public void start() {
        this.task = Scheduler.get().runTimer(() -> {
            MinionTickEvent event = new MinionTickEvent();
            event.begin();
            for (MinionArea world : MinionWorldCache.worlds()) {
                world.forEachPos(position -> {
                    if (!position.isTicking()) {
                        return;
                    }

                    this.tickChunk(position);
                });
            }
            event.commit();
        }, 1, Config.tickFrequency);
    }

    private void tickChunk(ChunkPos position) {
        ObjectArrayList<Minion> minions = position.minions();
        int minionSize;
        Minion first;
        synchronized (minions) {
            minionSize = minions.size();
            if (minionSize == 0) {
                return;
            }

            first = minions.getFirst();
        }

        Scheduler.get().executeAt(first.location(), () -> {
            // We already have the first, why not tick it?
            first.tick();
            synchronized (minions) {
                int newSize = minions.size();
                for (int i = 1; i < newSize; i++) {
                    Minion minion = minions.get(i);
                    minion.tick();
                }
            }
        });
    }

    @Override
    public void cancel() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}
