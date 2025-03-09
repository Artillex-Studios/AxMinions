package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axminions.minions.actions.collectors.implementation.BlockCollector;
import com.artillexstudios.axminions.minions.actions.collectors.implementation.EntityCollector;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import com.artillexstudios.axminions.utils.QuadFunction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import redempt.crunch.CompiledExpression;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class CollectorRegistry {
    private static final HashMap<String, Pair<Class<?>, QuadFunction<Collector<?>, CollectorShape, CompiledExpression, List<Filter<?>>, List<Requirement>>>> registry = new HashMap<>();
    private static final Collection<String> KEYS = Collections.unmodifiableCollection(registry.keySet());

    static {
        register("block", Location.class, BlockCollector::new);
        register("entity", Entity.class, EntityCollector::new);
    }

    public static void register(String id, Class<?> clazz, QuadFunction<Collector<?>, CollectorShape, CompiledExpression, List<Filter<?>>, List<Requirement>> function) {
        registry.put(id.toLowerCase(Locale.ENGLISH), Pair.of(clazz, function));
    }

    public static boolean exists(String id) {
        return registry.containsKey(id.toLowerCase(Locale.ENGLISH));
    }

    public static Class<?> getCollectedClass(String id) {
        return registry.get(id.toLowerCase(Locale.ENGLISH)).getKey();
    }

    public static Collector<?> get(String id, CollectorShape shape, CompiledExpression expression, List<Filter<?>> filters, List<Requirement> requirements) {
        return registry.get(id.toLowerCase(Locale.ENGLISH)).getValue().apply(shape, expression, filters, requirements);
    }

    public static Collection<String> keys() {
        return KEYS;
    }
}
