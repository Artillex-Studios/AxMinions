package com.artillexstudios.axminions.minions.actions.requirements;

import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.requirements.implementation.RequirementContainer;
import com.artillexstudios.axminions.minions.actions.requirements.implementation.RequirementLevel;
import com.artillexstudios.axminions.minions.actions.requirements.implementation.RequirementTool;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public final class Requirements {
    private static final HashMap<String, BiFunction<Map<Object, Object>, List<Effect<Object, Object>>, Requirement>> requirements = new HashMap<>();

    static {
        register("level", RequirementLevel::new);
        register("tool", RequirementTool::new);
        register("container", RequirementContainer::new);
    }

    public static void register(String id, BiFunction<Map<Object, Object>, List<Effect<Object, Object>>, Requirement> supplier) {
        requirements.put(id.toLowerCase(Locale.ENGLISH), supplier);
    }

    public static Requirement parse(String id, Map<Object, Object> configuration, List<Effect<Object, Object>> elseEffect) {
        BiFunction<Map<Object, Object>, List<Effect<Object, Object>>, Requirement> effectSupplier = requirements.get(id.toLowerCase(Locale.ENGLISH));
        if (effectSupplier == null) {
            return null;
        }

        return effectSupplier.apply(configuration, elseEffect);
    }
}
