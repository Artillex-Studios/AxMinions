package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

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
            final int xStart = (int) Math.round(blockX - range);
            final int xEnd = (int) Math.round(blockX + range);
            final int yStart = (int) Math.round(blockY - range);
            final int yEnd = (int) Math.round(blockY + range);
            final int zStart = (int) Math.round(blockZ - range);
            final int zEnd = (int) Math.round(blockZ + range);

            for (int x = xStart; x <= xEnd; x++) {
                final int xDistance = (blockX - x) * (blockX - x);
                for (int y = yStart; y <= yEnd; y++) {
                    final int yDistance = (blockY - y) * (blockY - y);
                    z:
                    for (int z = zStart; z < zEnd; z++) {
                        final int zDistance = (blockZ - z) * (blockZ - z);
                        final int distance = xDistance + yDistance + zDistance;

                        if (distance < rangeSquared && distance < smallRangeSquared) {
                            try {
                                Location newLocation = new Location(location.getWorld(), x, y, z);
                                for (Filter<?> filter : filters) {
                                    if (!filter.isAllowed(newLocation)) {
                                        continue z;
                                    }
                                }

                                consumer.accept(newLocation);
                            } catch (MinionTickFailException exception) {
                                LogUtils.debug("Tick failed, aborting!");
                                return;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer) {
            for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                consumer.accept(nearbyEntity);
            }
        }
    },
    CIRCLE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {
            final int blockX = location.getBlockX();
            final int blockY = location.getBlockY();
            final int blockZ = location.getBlockZ();

            final double rangeSquared = range * range;
            final double smallRangeSquared = ((range - 1) * (range - 1));
            final int xStart = (int) Math.round(blockX - range);
            final int xEnd = (int) Math.round(blockX + range);
            final int zStart = (int) Math.round(blockZ - range);
            final int zEnd = (int) Math.round(blockZ + range);

            for (int x = xStart; x <= xEnd; x++) {
                final int xDistance = (blockX - x) * (blockX - x);
                z:
                for (int z = zStart; z < zEnd; z++) {
                    final int zDistance = (blockZ - z) * (blockZ - z);
                    final int distance = xDistance + zDistance;

                    if (distance < rangeSquared && distance < smallRangeSquared) {
                        try {
                            Location newLocation = new Location(location.getWorld(), x, blockY, z);
                            for (Filter<?> filter : filters) {
                                if (!filter.isAllowed(newLocation)) {
                                    continue z;
                                }
                            }

                            consumer.accept(newLocation);
                        } catch (MinionTickFailException exception) {
                            LogUtils.debug("Tick failed, aborting!");
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer) {
            for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                consumer.accept(nearbyEntity);
            }
        }
    },
    SQUARE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {
            final int blockX = location.getBlockX();
            final int blockY = location.getBlockY();
            final int blockZ = location.getBlockZ();

            final int xStart = (int) Math.round(blockX - range);
            final int xEnd = (int) Math.round(blockX + range);
            final int zStart = (int) Math.round(blockZ - range);
            final int zEnd = (int) Math.round(blockZ + range);

            for (int x = xStart; x <= xEnd; x++) {
                z:
                for (int z = zStart; z <= zEnd; z++) {
                    try {
                        Location newLocation = new Location(location.getWorld(), x, blockY, z);
                        for (Filter<?> filter : filters) {
                            if (!filter.isAllowed(newLocation)) {
                                continue z;
                            }
                        }

                        consumer.accept(newLocation);
                    } catch (MinionTickFailException exception) {
                        LogUtils.debug("Tick failed, aborting!");
                        return;
                    }
                }
            }
        }

        @Override
        public void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer) {
            for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                consumer.accept(nearbyEntity);
            }
        }
    },
    CUBE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {
            final int blockX = location.getBlockX();
            final int blockY = location.getBlockY();
            final int blockZ = location.getBlockZ();

            final int xStart = (int) Math.round(blockX - range);
            final int xEnd = (int) Math.round(blockX + range);
            final int yStart = (int) Math.round(blockY - range);
            final int yEnd = (int) Math.round(blockY + range);
            final int zStart = (int) Math.round(blockZ - range);
            final int zEnd = (int) Math.round(blockZ + range);

            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    z:
                    for (int z = zStart; z <= zEnd; z++) {
                        try {
                            Location newLocation = new Location(location.getWorld(), x, y, z);
                            for (Filter<?> filter : filters) {
                                if (!filter.isAllowed(newLocation)) {
                                    continue z;
                                }
                            }

                            consumer.accept(newLocation);
                        } catch (MinionTickFailException exception) {
                            LogUtils.debug("Tick failed, aborting!");
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer) {
            for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                consumer.accept(nearbyEntity);
            }
        }
    },
    FACE {
        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }

        @Override
        public void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer) {
            for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                consumer.accept(nearbyEntity);
            }
        }
    },
    LINE {
        private static final BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        @Override
        public void getBlocks(Location location, double range, List<Filter<?>> filters, Consumer<Location> consumer) {

        }

        @Override
        public void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer) {
            for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                consumer.accept(nearbyEntity);
            }
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

    public abstract void getEntities(Location location, double range, List<Filter<?>> filters, Consumer<Entity> consumer);
}
