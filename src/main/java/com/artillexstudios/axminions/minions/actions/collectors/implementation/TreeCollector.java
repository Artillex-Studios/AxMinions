package com.artillexstudios.axminions.minions.actions.collectors.implementation;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorOptionNotPresentException;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import redempt.crunch.CompiledExpression;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public final class TreeCollector extends Collector<Location> {
    private static final Vector[] vectors = new Vector[]{BlockFace.UP.getDirection(), BlockFace.DOWN.getDirection(), BlockFace.SOUTH.getDirection(), BlockFace.EAST.getDirection(), BlockFace.NORTH.getDirection(), BlockFace.WEST.getDirection()};

    public TreeCollector(CollectorContext context, List<Requirement> requirements) {
        super(context, requirements);
    }

    @Override
    public Class<?> getCollectedClass() {
        return Location.class;
    }

    @Override
    public void collect(Minion minion, Consumer<Location> consumer) {
        if (Config.debug) {
            LogUtils.debug("TreeCollector run!");
        }
        CompiledExpression limitExpression = this.context.optionOrDefault(CollectorOptions.LIMIT_RAW, Collector.ZERO_EXPRESSION);
        CompiledExpression rangeExpression = this.context.option(CollectorOptions.RANGE_RAW);

        final Queue<Location> queue = new ArrayDeque<>();
        final Set<Location> visited = new HashSet<>();
        try {
            this.context.option(CollectorOptions.SHAPE).getBlocks(this.context.toBuilder()
                    .withOption(CollectorOptions.LIMIT, limitExpression.getVariableCount() == 0 ? (int) limitExpression.evaluate() : (int) limitExpression.evaluate(minion.level().id()))
                    .withOption(CollectorOptions.RANGE, rangeExpression.getVariableCount() == 0 ? rangeExpression.evaluate() : rangeExpression.evaluate(minion.level().id()))
                    .withOption(CollectorOptions.LOCATION, minion.location())
                    .withOption(CollectorOptions.FACING, minion.facing().blockFace())
                    .withOption(CollectorOptions.LOCATION_CONSUMER, blockLocation -> {
                        if (Config.debug) {
                            LogUtils.debug("Location: {}", blockLocation);
                        }
                        int counter = 0;
                        queue.add(blockLocation);
                        visited.add(blockLocation);

                        Location queuedLocation;
                        while ((queuedLocation = queue.peek()) != null) {
                            for (Filter<?> filter : this.context.option(CollectorOptions.FILTERS)) {
                                if (!filter.isAllowed(queuedLocation)) {
                                    continue;
                                }

                                if (counter >= Config.treeCollectorMaxCollected) {
                                    return;
                                }
                                counter++;

                                consumer.accept(queuedLocation);

                                for (Vector vector : vectors) {
                                    Location nearbyLocation = queuedLocation.add(vector);
                                    if (visited.add(nearbyLocation)) {
                                        queue.add(nearbyLocation);
                                    }
                                }
                                break;
                            }
                        }
                    })
                    .build()
            );
        } catch (CollectorOptionNotPresentException exception) {
            LogUtils.warn("Failed to tick minion due to collector option {} not being present! This is not a configuration issue, but an issue in the code! Report to the developer!", exception.option());
        }
    }
}
