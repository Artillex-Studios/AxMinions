package com.artillexstudios.axminions.minions.actions.collectors.shapes;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorOptionNotPresentException;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.function.Consumer;

public final class SphereCollectorShape extends CollectorShape {

    @Override
    public void getBlocks(CollectorContext context) throws CollectorOptionNotPresentException {
        if (Config.debug) {
            LogUtils.debug("Getting blocks!");
        }
        Location location = context.optionOrThrow(CollectorOptions.LOCATION);
        double range = context.optionOrThrow(CollectorOptions.RANGE);
        int limit = context.optionOrDefault(CollectorOptions.LIMIT, 0);
        List<Filter<?>> filters = context.optionOrThrow(CollectorOptions.FILTERS);
        Consumer<Location> consumer = context.optionOrThrow(CollectorOptions.LOCATION_CONSUMER);

        final int blockX = location.getBlockX();
        final int blockY = location.getBlockY();
        final int blockZ = location.getBlockZ();
        final World world = location.getWorld();

        final double rangeSquared = range * range;
        final double smallRangeSquared = ((range - 1) * (range - 1));
        final int xStart = (int) Math.round(blockX - range);
        final int xEnd = (int) Math.round(blockX + range);
        final int yStart = (int) Math.round(blockY - range);
        final int yEnd = (int) Math.round(blockY + range);
        final int zStart = (int) Math.round(blockZ - range);
        final int zEnd = (int) Math.round(blockZ + range);

        final Location newLocation = new Location(world, 0, 0, 0);
        int successful = 0;
        for (int x = xStart; x <= xEnd; x++) {
            final int xDistance = (blockX - x) * (blockX - x);
            for (int y = yStart; y <= yEnd; y++) {
                final int yDistance = (blockY - y) * (blockY - y);
                z:
                for (int z = zStart; z < zEnd; z++) {
                    final int zDistance = (blockZ - z) * (blockZ - z);
                    final int distance = xDistance + yDistance + zDistance;

                    if (distance < rangeSquared) {
                        try {
                            newLocation.setX(x);
                            newLocation.setY(y);
                            newLocation.setZ(z);
                            for (Filter<?> filter : filters) {
                                if (!filter.isAllowed(newLocation)) {
                                    if (Config.debug) {
                                        LogUtils.debug("Not allowed! Filter: {}", filter);
                                    }
                                    continue z;
                                }
                            }

                            consumer.accept(newLocation);
                            successful++;
                            if (successful == limit) {
                                if (Config.debug) {
                                    LogUtils.debug("Limit reached");
                                }
                                // Limit reached, no need to do anything with the rest
                                return;
                            }
                        } catch (MinionTickFailException exception) {
                            if (Config.debug) {
                                LogUtils.debug("Tick failed, aborting!");
                            }
                            throw exception;
                        }
                    } else {
                        if (Config.debug) {
                            LogUtils.debug("Block is not in range!");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void getEntities(CollectorContext context) throws CollectorOptionNotPresentException {
        Location location = context.optionOrThrow(CollectorOptions.LOCATION);
        double range = context.optionOrThrow(CollectorOptions.RANGE);
        int limit = context.optionOrDefault(CollectorOptions.LIMIT, 0);
        List<Filter<?>> filters = context.optionOrThrow(CollectorOptions.FILTERS);
        Consumer<Entity> consumer = context.optionOrThrow(CollectorOptions.ENTITY_CONSUMER);

        int successful = 0;
        for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            for (Filter<?> filter : filters) {
                if (!filter.isAllowed(nearbyEntity)) {
                    break;
                }
            }

            consumer.accept(nearbyEntity);
            successful++;
            if (successful == limit) {
                // Limit reached, no need to do anything with the rest
                return;
            }
        }
    }
}
