package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axapi.reflection.FastMethodInvoker;
import com.artillexstudios.axminions.minions.actions.collectors.implementation.BlockCollector;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import redempt.crunch.CompiledExpression;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class CollectorRegistry {
    private static final HashMap<String, FastMethodInvoker.TriFunction<Collector<?>, CollectorShape, CompiledExpression, List<Filter<?>>>> registry = new HashMap<>();
    private static final Collection<String> KEYS = Collections.unmodifiableCollection(registry.keySet());

    static {
        register("block", BlockCollector::new);
    }

    public static void register(String id, FastMethodInvoker.TriFunction<Collector<?>, CollectorShape, CompiledExpression, List<Filter<?>>> function) {
        registry.put(id.toLowerCase(Locale.ENGLISH), function);
    }

    public static boolean exists(String id) {
        return registry.containsKey(id.toLowerCase(Locale.ENGLISH));
    }

    public static Collector<?> get(String id, CollectorShape shape, CompiledExpression expression, List<Filter<?>> filters) {
        return registry.get(id.toLowerCase(Locale.ENGLISH)).apply(shape, expression, filters);
    }

    public static Collection<String> keys() {
        return KEYS;
    }
}
