package com.artillexstudios.axminions.minions.actions.collectors.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorShape;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import org.bukkit.Location;
import redempt.crunch.CompiledExpression;

import java.util.List;

public class BlockCollector extends Collector<Location> {
    private final CollectorShape shape;
    private final CompiledExpression expression;
    private final List<Filter<?>> filters;

    public BlockCollector(CollectorShape shape, CompiledExpression expression, List<Filter<?>> filters) {
        this.shape = shape;
        this.expression = expression;
        this.filters = filters;
    }

    @Override
    public Class<?> getCollectedClass() {
        return Location.class;
    }

    @Override
    public List<Location> collect(Minion minion) {
        double radius = expression.getVariableCount() == 0 ? expression.evaluate() : expression.evaluate(minion.level().id());
        Location location = minion.location();

        // TODO: Filters
        return shape.getBlocks(location, radius);
    }
}
