package com.artillexstudios.axminions.minions.actions.collectors;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.List;
import java.util.Optional;

public enum CollectorShape {
    SPHERE {
        @Override
        public List<Location> getBlocks(Location location, final double range) {
            int preSize = (int) (3.45 * range * range * range);
            final ObjectArrayList<Location> list = new ObjectArrayList<>(preSize);
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
                            list.add(new Location(location.getWorld(), x, y, z));
                        }
                    }
                }
            }

            return list;
        }
    },
    CIRCLE {
        @Override
        public List<Location> getBlocks(Location location, double range) {
            return List.of();
        }
    },
    SQUARE {
        @Override
        public List<Location> getBlocks(Location location, double range) {
            return List.of();
        }
    },
    CUBE {
        @Override
        public List<Location> getBlocks(Location location, double range) {
            return List.of();
        }
    },
    FACE {
        @Override
        public List<Location> getBlocks(Location location, double range) {
            return List.of();
        }
    },
    LINE {
        private static final BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        @Override
        public List<Location> getBlocks(Location location, double range) {
            return List.of();
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

    public abstract List<Location> getBlocks(Location location, double range);
}
