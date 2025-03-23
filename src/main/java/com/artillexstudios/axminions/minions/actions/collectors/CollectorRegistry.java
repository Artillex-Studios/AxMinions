package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axminions.minions.actions.collectors.implementation.BlockCollector;
import com.artillexstudios.axminions.minions.actions.collectors.implementation.EntityCollector;
import com.artillexstudios.axminions.minions.actions.collectors.implementation.TreeCollector;
import com.artillexstudios.axminions.minions.actions.collectors.shapes.CollectorShape;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

public final class CollectorRegistry {
    private static final HashMap<String, Pair<Class<?>, BiFunction<CollectorContext, List<Requirement>, Collector<?>>>> registry = new HashMap<>();
    private static final Collection<String> KEYS = Collections.unmodifiableCollection(registry.keySet());

    static {
        register("block", Location.class, BlockCollector::new);
        register("tree", Location.class, TreeCollector::new);
        register("entity", Entity.class, EntityCollector::new);
    }

    public static void register(String id, Class<?> clazz, BiFunction<CollectorContext, List<Requirement>, Collector<?>> function) {
        registry.put(id.toLowerCase(Locale.ENGLISH), Pair.of(clazz, function));
    }

    public static boolean exists(String id) {
        return registry.containsKey(id.toLowerCase(Locale.ENGLISH));
    }

    public static Class<?> getCollectedClass(String id) {
        return registry.get(id.toLowerCase(Locale.ENGLISH)).getKey();
    }

    public static Collector<?> get(String id, CollectorContext.Builder builder, List<Requirement> requirements) {
        return registry.get(id.toLowerCase(Locale.ENGLISH)).getValue().apply(builder.build(), requirements);
    }

    public static Collection<String> keys() {
        return KEYS;
    }
}
