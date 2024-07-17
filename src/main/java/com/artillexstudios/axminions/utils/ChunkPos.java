package com.artillexstudios.axminions.utils;

import com.artillexstudios.axminions.minions.Minion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.atomic.AtomicBoolean;

public record ChunkPos(int x, int z, AtomicBoolean ticking, ObjectArrayList<Minion> minions) {

    public void ticking(boolean ticking) {
        this.ticking.set(ticking);
    }

    public boolean isTicking() {
        return this.ticking.get();
    }

    public void addMinion(Minion minion) {
        this.minions.add(minion);
    }

    public boolean removeMinion(Minion minion) {
        this.minions.remove(minion);

        return this.minions.isEmpty();
    }
}
