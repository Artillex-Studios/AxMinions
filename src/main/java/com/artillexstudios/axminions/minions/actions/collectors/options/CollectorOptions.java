package com.artillexstudios.axminions.minions.actions.collectors.options;

import com.artillexstudios.axminions.minions.actions.collectors.shapes.CollectorShape;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import redempt.crunch.CompiledExpression;

import java.util.List;
import java.util.function.Consumer;

public final class CollectorOptions {
    public static final CollectorOption<Consumer<Entity>> ENTITY_CONSUMER = new CollectorOption<>("entity_consumer");
    public static final CollectorOption<List<Filter<?>>> FILTERS = new CollectorOption<>("filters");
    public static final CollectorOption<String> COLLECTOR_ID = new CollectorOption<>("collector_id");
    public static final CollectorOption<CompiledExpression> LIMIT_RAW = new CollectorOption<>("limit_raw");
    public static final CollectorOption<Integer> LIMIT = new CollectorOption<>("limit");
    public static final CollectorOption<Location> LOCATION = new CollectorOption<>("location");
    public static final CollectorOption<Consumer<Location>> LOCATION_CONSUMER = new CollectorOption<>("location_consumer");
    public static final CollectorOption<CompiledExpression> RANGE_RAW = new CollectorOption<>("range_raw");
    public static final CollectorOption<Double> RANGE = new CollectorOption<>("range");
    public static final CollectorOption<BlockFace> FACING = new CollectorOption<>("facing");
    public static final CollectorOption<CollectorShape> SHAPE = new CollectorOption<>("shape");
}
