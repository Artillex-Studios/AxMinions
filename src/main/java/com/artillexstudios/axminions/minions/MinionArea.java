package com.artillexstudios.axminions.minions;

import com.artillexstudios.axminions.utils.ChunkPos;
import com.artillexstudios.axminions.utils.LogUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public final class MinionArea {
    private final ObjectArrayList<ChunkPos> positions = new ObjectArrayList<>(32);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private long readCount = 0;
    private long writeCount = 0;

    private static boolean isSameBlock(int x, int y, int z, Location loc2) {
        return x == loc2.getBlockX() &&
                y == loc2.getBlockY() &&
                z == loc2.getBlockZ();
    }

    public void startTicking(Chunk chunk) {
        // TODO: Create a queue of chunks that have started ticking. Since we load the minions async, the chunks might be loaded by the time the minions load in them
        LogUtils.debug("Chunk ticking x: {} z: {} world: {}", chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        ChunkPos pos = this.forChunk(chunk);

        if (pos != null && !pos.isTicking()) {
            LogUtils.debug("Starting chunk ticking!");
            pos.ticking(true);
        }
    }

    public void stopTicking(Chunk chunk) {
        LogUtils.debug("Chunk ticking stop x: {} z: {} world: {}", chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        ChunkPos pos = this.forChunk(chunk);

        if (pos != null && pos.isTicking()) {
            LogUtils.debug("Stopping chunk ticking!");
            pos.ticking(false);
        }
    }

    public void load(Minion minion) {
        Preconditions.checkNotNull(minion, "Minion is null!");
        Location location = minion.location();

        int chunkX = (int) Math.floor(location.getX()) >> 4;
        int chunkZ = (int) Math.floor(location.getZ()) >> 4;

        ChunkPos chunkPos = this.forChunk(chunkX, chunkZ);
        if (chunkPos == null) {
            chunkPos = new ChunkPos(chunkX, chunkZ, new AtomicBoolean(), new ObjectArrayList<>());

            this.writeLock.lock();
            try {
                this.writeCount++;
                this.positions.add(chunkPos);
            } finally {
                this.writeLock.unlock();
            }
        }

        chunkPos.addMinion(minion);
    }

    // Utility method to avoid acquiring lock lots of times on initial load
    public void loadAll(List<Minion> minions) {
        Preconditions.checkNotNull(minions, "Minions are null!");
        this.writeLock.lock();
        try {
            this.writeCount++;
            for (Minion minion : minions) {
                Location location = minion.location();

                int chunkX = (int) Math.floor(location.getX()) >> 4;
                int chunkZ = (int) Math.floor(location.getZ()) >> 4;

                ChunkPos chunkPos = this.forChunk(chunkX, chunkZ);
                if (chunkPos == null) {
                    chunkPos = new ChunkPos(chunkX, chunkZ, new AtomicBoolean(), new ObjectArrayList<>());

                    this.positions.add(chunkPos);
                }

                chunkPos.addMinion(minion);
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    public void remove(Minion minion) {
        Preconditions.checkNotNull(minion, "Minion is null!");
        Location location = minion.location();
        int chunkX = (int) Math.floor(location.getX()) >> 4;
        int chunkZ = (int) Math.floor(location.getZ()) >> 4;

        // Load the list into stack memory for faster access
        ObjectArrayList<ChunkPos> positions = this.positions;

        boolean needsWrite = false;
        this.readLock.lock();
        try {
            this.readCount++;
            ObjectListIterator<ChunkPos> positionIterator = positions.iterator();
            while (positionIterator.hasNext()) {
                ChunkPos nextPos = positionIterator.next();
                if (nextPos.x() == chunkX && nextPos.z() == chunkZ) {
                    if (nextPos.removeMinion(minion)) {
                        needsWrite = true;
                        positionIterator.remove();
                    }

                    break;
                }
            }
        } finally {
            this.readLock.unlock();
        }

        if (needsWrite) {
            this.writeLock.lock();
            try {
                this.writeCount++;
                ObjectListIterator<ChunkPos> positionIterator = positions.iterator();
                while (positionIterator.hasNext()) {
                    ChunkPos nextPos = positionIterator.next();
                    if (nextPos.x() == chunkX && nextPos.z() == chunkZ) {
                        positionIterator.remove();
                        break;
                    }
                }
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    public Minion getMinionAt(Location location) {
        Preconditions.checkNotNull(location, "Location is null!");
        int x = (int) Math.floor(location.getX());
        int y = (int) Math.floor(location.getY());
        int z = (int) Math.floor(location.getZ());

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        this.readLock.lock();
        try {
            this.readCount++;
            ObjectArrayList<ChunkPos> positions = this.positions;
            int posSize = positions.size();

            for (int i = 0; i < posSize; i++) {
                ChunkPos pos = positions.get(i);
                if (!pos.isTicking()) continue;
                if (pos.x() != chunkX || pos.z() != chunkZ) continue;

                ObjectArrayList<Minion> minions = pos.minions();
                int minionsSize = minions.size();
                for (int j = 0; j < minionsSize; j++) {
                    Minion minion = minions.get(j);
                    if (isSameBlock(x, y, z, minion.location())) {
                        return minion;
                    }
                }
            }

            return null;
        } finally {
            this.readLock.unlock();
        }
    }

    public List<Minion> getMinions(Chunk chunk) {
        return List.copyOf(this.forChunk(chunk).minions());
    }

    public ChunkPos forChunk(Chunk chunk) {
        return this.forChunk(chunk.getX(), chunk.getZ());
    }

    public ChunkPos forChunk(int x, int z) {
        this.readLock.lock();
        try {
            this.readCount++;
            // Load the list into stack memory for faster access
            ObjectArrayList<ChunkPos> positions = this.positions;

            // We don't want to reevaluate the size of the list
            int size = positions.size();
            for (int i = 0; i < size; i++) {
                ChunkPos pos = positions.get(i);
                if (pos.x() == x && pos.z() == z) {
                    return pos;
                }
            }

            return null;
        } finally {
            this.readLock.unlock();
        }
    }

    public void forEachPos(Consumer<ChunkPos> consumer) {
        this.readLock.lock();
        try {
            this.readCount++;
            // Load the list into stack memory for faster access
            ObjectArrayList<ChunkPos> positions = this.positions;

            // We don't want to reevaluate the size of the list
            int size = positions.size();
            for (int i = 0; i < size; i++) {
                consumer.accept(positions.get(i));
            }
        } finally {
            this.readLock.unlock();
        }
    }

    public long readCount() {
        return readCount;
    }

    public long writeCount() {
        return writeCount;
    }
}
