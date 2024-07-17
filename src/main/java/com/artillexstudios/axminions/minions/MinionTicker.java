package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.scheduler.ScheduledTask;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axminions.jfr.MinionTickEvent;
import com.artillexstudios.axminions.utils.ChunkPos;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class MinionTicker {
    private static ScheduledTask task;
    private static volatile boolean midTick = false;

    public static void start() {
        // TODO: Less loops
        task = Scheduler.get().runTimer(() -> {
            MinionTickEvent event = new MinionTickEvent();
            midTick = true;
            event.begin();
            for (MinionArea world : MinionWorldCache.worlds()) {
                world.forEachPos(position -> {
                    if (!position.isTicking()) return;

                    tickChunk(position);
                });
            }
            event.commit();
            midTick = false;
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

    public static boolean midTick() {
        return midTick;
    }

    public static void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}
