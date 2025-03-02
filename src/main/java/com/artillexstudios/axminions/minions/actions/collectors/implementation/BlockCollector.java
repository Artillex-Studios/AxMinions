package com.artillexstudios.axminions.minions.actions.collectors.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorShape;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import org.bukkit.Location;
import redempt.crunch.CompiledExpression;

import java.util.List;
import java.util.function.Consumer;

public final class BlockCollector extends Collector<Location> {

    public BlockCollector(CollectorShape shape, CompiledExpression expression, List<Filter<?>> filters, List<Requirement> requirements) {
        super(shape, expression, filters, requirements);
    }

    @Override
    public Class<?> getCollectedClass() {
        return Location.class;
    }

    @Override
    public void collect(Minion minion, Consumer<Location> consumer) {
        double radius = this.expression.getVariableCount() == 0 ? this.expression.evaluate() : this.expression.evaluate(minion.level().id());
        Location location = minion.location();

        this.shape.getBlocks(location, radius, this.filters, consumer);
    }
}
