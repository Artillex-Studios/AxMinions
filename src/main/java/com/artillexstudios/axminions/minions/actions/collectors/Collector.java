package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.utils.LogUtils;
import redempt.crunch.Crunch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Collector<T> {

    public static Collector<?> of(Map<Object, Object> config) {
        LogUtils.debug("Collector of {}", config);
        if (config == null) {
            return null;
        }

        String collectorID = (String) config.get("id");
        if (collectorID == null) {
            LogUtils.warn("Collector id was not defined!");
            return null;
        }

        if (!CollectorRegistry.exists(collectorID)) {
            LogUtils.warn("Collector id {} not present! Collector ids: {}", collectorID, CollectorRegistry.keys());
            return null;
        }

        String shape = (String) config.get("shape");
        if (shape == null) {
            LogUtils.warn("Shape was not defined!");
            return null;
        }

        Optional<CollectorShape> collectorShape = CollectorShape.parse(shape);
        if (collectorShape.isEmpty()) {
            LogUtils.warn("Invalid shape! Shapes: {}", Arrays.toString(CollectorShape.entries));
            return null;
        }

        String radius = (String) config.get("radius");
        if (radius == null) {
            LogUtils.warn("Radius was not defined!");
            return null;
        }

        // TODO: Filters
        return CollectorRegistry.get(collectorID, collectorShape.get(), Crunch.compileExpression(radius.replace("<level>", "$1")), List.of());
    }

    public abstract Class<?> getCollectedClass();

    public abstract List<T> collect(Minion minion);
}
