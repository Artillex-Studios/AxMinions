package com.artillexstudios.axminions.minions;

import com.artillexstudios.axapi.utils.logging.DebugMode;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.utils.ChunkPos;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class MinionArea {
    private final ConcurrentLinkedQueue<ChunkPos> positions = new ConcurrentLinkedQueue<>();

    private static boolean isSameBlock(int x, int y, int z, Location loc2) {
        return x == loc2.getBlockX() &&
                y == loc2.getBlockY() &&
                z == loc2.getBlockZ();
    }

    public void startTicking(Chunk chunk) {
        if (Config.debug) {
            LogUtils.debug("Chunk ticking x: {} z: {} world: {}", chunk.getX(), chunk.getZ(), chunk.getWorld().getName(), DebugMode.FILE);
        }
        ChunkPos pos = this.forChunk(chunk);

        if (pos != null && !pos.isTicking()) {
            if (Config.debug) {
                LogUtils.debug("Starting chunk ticking!", DebugMode.CONSOLE);
            }
            pos.ticking(true);
        }
    }

    public void stopTicking(Chunk chunk) {
        if (Config.debug) {
            LogUtils.debug("Chunk ticking stop x: {} z: {} world: {}", chunk.getX(), chunk.getZ(), chunk.getWorld().getName(), DebugMode.FILE);
        }
        ChunkPos pos = this.forChunk(chunk);

        if (pos != null && pos.isTicking()) {
            if (Config.debug) {
                LogUtils.debug("Stopping chunk ticking!", DebugMode.CONSOLE);
            }
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
            this.positions.add(chunkPos);
        }

        chunkPos.addMinion(minion);
    }

    // Utility method to avoid acquiring lock lots of times on initial load
    public void loadAll(List<Minion> minions) {
        Preconditions.checkNotNull(minions, "Minions are null!");
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
    }

    public void remove(Minion minion) {
        Preconditions.checkNotNull(minion, "Minion is null!");
        Location location = minion.location();
        int chunkX = (int) Math.floor(location.getX()) >> 4;
        int chunkZ = (int) Math.floor(location.getZ()) >> 4;


        Iterator<ChunkPos> positionIterator = this.positions.iterator();
        while (positionIterator.hasNext()) {
            ChunkPos nextPos = positionIterator.next();
            if (nextPos.x() == chunkX && nextPos.z() == chunkZ) {
                if (nextPos.removeMinion(minion)) {
                    positionIterator.remove();
                }

                break;
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

        for (ChunkPos position : this.positions) {
            if (!position.isTicking()) {
                continue;
            }

            if (position.x() != chunkX || position.z() != chunkZ) {
                continue;
            }

            synchronized (position.minions()) {
                ObjectArrayList<Minion> minions = position.minions();
                int minionsSize = minions.size();
                for (int j = 0; j < minionsSize; j++) {
                    Minion minion = minions.get(j);
                    if (isSameBlock(x, y, z, minion.location())) {
                        return minion;
                    }
                }
            }
        }

        return null;
    }

    public List<Minion> getMinions(Chunk chunk) {
        List<Minion> minions = this.forChunk(chunk).minions();
        synchronized (minions) {
            return List.copyOf(minions);
        }
    }

    public ChunkPos forChunk(Chunk chunk) {
        return this.forChunk(chunk.getX(), chunk.getZ());
    }

    public ChunkPos forChunk(int x, int z) {
        for (ChunkPos position : this.positions) {
            if (position.x() == x && position.z() == z) {
                return position;
            }
        }

        return null;
    }

    public void forEachPos(Consumer<ChunkPos> consumer) {
        for (ChunkPos position : this.positions) {
            consumer.accept(position);
        }
    }

    public void clear() {
        for (ChunkPos position : this.positions) {
            synchronized (position.minions()) {
                position.minions().clear();
            }
        }
    }
}
