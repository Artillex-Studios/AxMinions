package com.artillexstudios.axminions.minions.ticker;

import com.artillexstudios.axapi.scheduler.ScheduledTask;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axminions.jfr.MinionTickEvent;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.MinionArea;
import com.artillexstudios.axminions.minions.MinionTicker;
import com.artillexstudios.axminions.minions.MinionWorldCache;
import com.artillexstudios.axminions.utils.ChunkPos;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

// TODO: Add a list with all minions, add a ticking boolean to minions
public final class BukkitMinionTicker implements MinionTicker {
    private static ScheduledTask task;

    @Override
    public void start() {
        task = Scheduler.get().runTimer(() -> {
            MinionTickEvent event = new MinionTickEvent();
            event.begin();
            for (MinionArea world : MinionWorldCache.worlds()) {
                world.forEachPos(position -> {
                    if (!position.isTicking()) return;

                    tickChunk(position);
                });
            }
            event.commit();
        }, 1, 1);
    }

    private static void tickChunk(ChunkPos position) {
        ObjectArrayList<Minion> minions = position.minions();
        int minionSize = minions.size();
        if (minionSize == 0) return;

        Minion first = minions.get(0);
        Scheduler.get().executeAt(first.location(), () -> {
            // We already have the first, why not tick it?
            first.tick();

            for (int i = 1; i < minionSize; i++) {
                Minion minion = minions.get(i);
                minion.tick();
            }
        });
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}
