package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public enum CollectorShape {
    SPHERE {
        @Override
        public void getBlocks(Location location, final double range, List<Filter<?>> filters, Consumer<Location> consumer) {
            final int blockX = location.getBlockX();
            final int blockY = location.getBlockY();
            final int blockZ = location.getBlockZ();

            final double rangeSquared = range * range;
            final double smallRangeSquared = ((range - 1) * (range - 1));
            final double xStart = blockX - range;
            final double xEnd = blockX + range;
            final double yStart = blockY - range;
            final double yEnd = blockY + range;
            final double zStart = blockZ - range;
            final double zEnd = blockZ + range;

            for (double x = xStart; x <= xEnd; x++) {
                final double xDistance = (blockX - x) * (blockX - x);
                for (double y = yStart; y <= yEnd; y++) {
                    final double yDistance = (blockY - y) * (blockY - y);
                    for (double z = zStart; z < zEnd; z++) {
                        final double zDistance = (blockZ - z) * (blockZ - z);
                        final double distance = xDistance + yDistance + zDistance;

                        if (distance < rangeSquared && distance < smallRangeSquared) {
                            try {
                                consumer.accept(new Location(location.getWorld(), x, y, z));
                            } catch (MinionTickFailException exception) {
                                LogUtils.debug("Tick failed, aborting!");
                                return;
                            }
                        }
                    }
                }
            }
        }
    },
    CIRCLE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }
    },
    SQUARE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }
    },
    CUBE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }
    },
    FACE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }
    },
    LINE {
        private static final BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }
    };

    public static final CollectorShape[] entries = CollectorShape.values();

    public static Optional<CollectorShape> parse(String name) {
        for (CollectorShape entry : entries) {
            if (entry.name().equalsIgnoreCase(name)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public abstract void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer);
}
