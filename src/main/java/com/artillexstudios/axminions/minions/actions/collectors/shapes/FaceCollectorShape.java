package com.artillexstudios.axminions.minions.actions.collectors.shapes;

import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorOptionNotPresentException;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.function.Consumer;

public final class FaceCollectorShape extends CollectorShape {

    @Override
    public void getBlocks(CollectorContext context) throws CollectorOptionNotPresentException {
        Location location = context.optionOrThrow(CollectorOptions.LOCATION);
        int range = (int) Math.round(context.optionOrThrow(CollectorOptions.RANGE));
        int limit = context.optionOrDefault(CollectorOptions.LIMIT, 0);
        List<Filter<?>> filters = context.optionOrThrow(CollectorOptions.FILTERS);
        Consumer<Location> consumer = context.optionOrThrow(CollectorOptions.LOCATION_CONSUMER);
        BlockFace blockFace = context.optionOrThrow(CollectorOptions.FACING);

        int successful = 0;
        final Location newLocation = new Location(location.getWorld(), 0, 0, 0);
        for (int i = 1; i < range; i++) {
            newLocation.add(blockFace.getModX(), 0, blockFace.getModZ());
            for (Filter<?> filter : filters) {
                if (!filter.isAllowed(newLocation)) {
                    break;
                }
            }

            consumer.accept(newLocation);
            successful++;
            if (successful == limit) {
                // Limit reached, no need to do anything with the rest
                return;
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
