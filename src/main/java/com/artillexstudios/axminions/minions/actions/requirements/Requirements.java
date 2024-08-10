package com.artillexstudios.axminions.minions.actions.requirements;

import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.EntityTypeFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.MaterialFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.StoneGeneratorFilter;
import com.artillexstudios.axminions.minions.actions.requirements.implementation.RequirementLevel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class Requirements {
    private static final HashMap<String, Function<Map<Object, Object>, Requirement>> requirements = new HashMap<>();

    static {
        register("level", RequirementLevel::new);
    }

    public static void register(String id, Function<Map<Object, Object>, Requirement> supplier) {
        requirements.put(id.toLowerCase(Locale.ENGLISH), supplier);
    }

    public static Requirement parse(String id, Map<Object, Object> configuration) {
        Function<Map<Object, Object>, Requirement> effectSupplier = requirements.get(id.toLowerCase(Locale.ENGLISH));
        if (effectSupplier == null) {
            return null;
        }

        return effectSupplier.apply(configuration);
    }
}
