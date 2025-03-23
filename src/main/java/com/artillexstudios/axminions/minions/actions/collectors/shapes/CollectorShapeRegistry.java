package com.artillexstudios.axminions.minions.actions.collectors.shapes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public final class CollectorShapeRegistry {
    private static final HashMap<String, Supplier<CollectorShape>> registry = new HashMap<>();
    private static final Collection<String> KEYS = Collections.unmodifiableCollection(registry.keySet());

    static {
        register("sphere", SphereCollectorShape::new);
        register("circle", CircleCollectorShape::new);
        register("cube", CubeCollectorShape::new);
        register("square", SquareCollectorShape::new);
        register("face", FaceCollectorShape::new);
        register("line", LineCollectorShape::new);
    }

    public static void register(String id, Supplier<CollectorShape> shape) {
        registry.put(id.toLowerCase(Locale.ENGLISH), shape);
    }

    public static boolean exists(String id) {
        return registry.containsKey(id.toLowerCase(Locale.ENGLISH));
    }

    public static Optional<CollectorShape> parse(String id) {
        Supplier<CollectorShape> supplier = registry.get(id.toLowerCase(Locale.ENGLISH));
        return supplier == null ? Optional.empty() : Optional.of(supplier.get());
    }

    public static Collection<String> keys() {
        return KEYS;
    }
}
