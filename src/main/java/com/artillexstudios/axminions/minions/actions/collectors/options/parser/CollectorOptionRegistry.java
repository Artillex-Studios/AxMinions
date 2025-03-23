package com.artillexstudios.axminions.minions.actions.collectors.options.parser;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.collectors.options.parser.exception.InvalidCollectorOptionException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CollectorOptionRegistry {
    private static final HashMap<String, CollectorOptionParser> registry = new HashMap<>();
    private static final Collection<String> KEYS = Collections.unmodifiableCollection(registry.keySet());

    static {
        register("limit_raw", new CompiledExpressionOptionParser(List.of("limit"), CollectorOptions.LIMIT_RAW, Pair.of("<level>", "$1")));
        register("range_raw", new CompiledExpressionOptionParser(List.of("range", "radius"), CollectorOptions.RANGE_RAW, Pair.of("<level>", "$1")));
        register("filters", new FiltersOptionParser());
        register("shape", new CollectorShapeParser());
    }

    public static void register(String id, CollectorOptionParser parser) {
        registry.put(id.toLowerCase(Locale.ENGLISH), parser);
    }

    public static boolean exists(String id) {
        return registry.containsKey(id.toLowerCase(Locale.ENGLISH));
    }

    public static void parseAll(Map<Object, Object> config, CollectorContext.Builder builder) throws InvalidCollectorOptionException {
        for (CollectorOptionParser value : registry.values()) {
            value.parse(config, builder);
        }
    }

    public static Collection<String> keys() {
        return KEYS;
    }
}
