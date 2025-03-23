package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.collectors.options.parser.exception.InvalidCollectorOptionException;
import com.artillexstudios.axminions.minions.actions.collectors.shapes.CollectorShape;
import com.artillexstudios.axminions.minions.actions.collectors.shapes.CollectorShapeRegistry;

import java.util.Map;
import java.util.Optional;

public final class CollectorShapeParser implements CollectorOptionParser {

    @Override
    public void parse(Map<Object, Object> config, CollectorContext.Builder builder) throws InvalidCollectorOptionException {
        String shape = (String) config.get("shape");
        if (shape == null) {
            LogUtils.warn("Shape was not defined!");
            throw new InvalidCollectorOptionException();
        }

        Optional<CollectorShape> collectorShape = CollectorShapeRegistry.parse(shape);
        if (collectorShape.isEmpty()) {
            LogUtils.warn("Invalid shape! Shapes: {}", String.join(", ", CollectorShapeRegistry.keys()));
            throw new InvalidCollectorOptionException();
        }

        builder.withOption(CollectorOptions.SHAPE, collectorShape.get());
    }
}
