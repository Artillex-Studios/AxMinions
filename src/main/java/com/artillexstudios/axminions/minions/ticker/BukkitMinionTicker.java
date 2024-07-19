package com.artillexstudios.axminions.minions.ticker;

import com.artillexstudios.axapi.scheduler.ScheduledTask;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axminions.jfr.MinionTickEvent;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionTicker;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class BukkitMinionTicker implements MinionTicker {
    private ScheduledTask task;

    @Override
    public void start() {
        this.task = Scheduler.get().runTimer(() -> {
            MinionTickEvent event = new MinionTickEvent();
            event.begin();

            final ObjectArrayList<Minion> minions = MinionWorldCache.minions();
            final int minionSize = minions.size();
            for (int i = 0; i < minionSize; i++) {
                final Minion minion = minions.get(i);
                if (!minion.ticking()) {
                    continue;
                }

                minion.tick();
            }
            event.commit();
        }, 1, 1);
    }


    @Override
    public void cancel() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}
