package com.artillexstudios.axminions.minions.actions.effects;

import com.artillexstudios.axminions.minions.actions.effects.implementation.AddToContainerEffect;
import com.artillexstudios.axminions.minions.actions.effects.implementation.BreakEffect;
import com.artillexstudios.axminions.minions.actions.effects.implementation.DamageEntityEffect;
import com.artillexstudios.axminions.minions.actions.effects.implementation.DropAtMinionEffect;
import com.artillexstudios.axminions.minions.actions.effects.implementation.SmeltEffect;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class Effects {
    private static final HashMap<String, Function<Map<Object, Object>, Effect<?, ?>>> effects = new HashMap<>();

    static {
        register("add_to_container", AddToContainerEffect::new);
        register("break", BreakEffect::new);
        register("drop_at_minion", DropAtMinionEffect::new);
        register("smelt", SmeltEffect::new);
        register("damage", DamageEntityEffect::new);
    }

    public static void register(String id, Function<Map<Object, Object>, Effect<?, ?>> supplier) {
        effects.put(id.toLowerCase(Locale.ENGLISH), supplier);
    }

    public static Effect<Object, Object> parse(String id, Map<Object, Object> configuration) {
        Function<Map<Object, Object>, Effect<?, ?>> effectSupplier = effects.get(id.toLowerCase(Locale.ENGLISH));
        if (effectSupplier == null) {
            return null;
        }

        return (Effect<Object, Object>) effectSupplier.apply(configuration);
    }
}
