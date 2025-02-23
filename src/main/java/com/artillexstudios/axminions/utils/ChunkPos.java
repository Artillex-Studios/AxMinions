package com.artillexstudios.axminions.utils;

import com.artillexstudios.axminions.minions.Minion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ChunkPos {
    private final int x;
    private final int z;
    private final AtomicBoolean ticking;
    private final ObjectArrayList<Minion> minions;

    public ChunkPos(int x, int z, AtomicBoolean ticking, ObjectArrayList<Minion> minions) {
        this.x = x;
        this.z = z;
        this.ticking = ticking;
        this.minions = minions;
    }

    public void ticking(boolean ticking) {
        this.ticking.set(ticking);

        for (Minion minion : this.minions) {
            minion.ticking(ticking);
        }
    }

    public boolean isTicking() {
        return this.ticking.get();
    }

    public void addMinion(Minion minion) {
        minion.ticking(this.ticking.get());

        synchronized (this.minions) {
            this.minions.add(minion);
        }
    }

    public boolean removeMinion(Minion minion) {
        synchronized (this.minions) {
            this.minions.remove(minion);

            return this.minions.isEmpty();
        }
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

    public ObjectArrayList<Minion> minions() {
        return this.minions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChunkPos) obj;
        return this.x == that.x &&
                this.z == that.z &&
                Objects.equals(this.ticking, that.ticking) &&
                Objects.equals(this.minions, that.minions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.z, this.ticking, this.minions);
    }

    @Override
    public String toString() {
        return "ChunkPos[" +
                "x=" + this.x + ", " +
                "z=" + this.z + ", " +
                "ticking=" + this.ticking + ", " +
                "minions=" + this.minions + ']';
    }

}
