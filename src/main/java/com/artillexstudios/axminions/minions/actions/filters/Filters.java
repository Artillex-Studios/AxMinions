package com.artillexstudios.axminions.minions.actions.filters;

import com.artillexstudios.axminions.minions.actions.filters.implementation.AnimalFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.EntityTypeFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.InvertedFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.MaterialFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.StoneGeneratorFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.TamedFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class Filters {
    private static final HashMap<String, Function<Map<Object, Object>, Filter<?>>> filters = new HashMap<>();

    static {
        register("material", MaterialFilter::new);
        register("stone_generator", StoneGeneratorFilter::new);
        register("entity", EntityTypeFilter::new);
        register("animal", AnimalFilter::new);
        register("tamed", TamedFilter::new);
    }

    public static void register(String id, Function<Map<Object, Object>, Filter<?>> supplier) {
        filters.put(id.toLowerCase(Locale.ENGLISH), supplier);
    }

    public static Filter<Object> parse(String id, Map<Object, Object> configuration) {
        boolean inverted = false;
        if (id.startsWith("!")) {
            inverted = true;
        }

        id = StringUtils.replaceOnce(id, "!", "");

        Function<Map<Object, Object>, Filter<?>> effectSupplier = filters.get(id.toLowerCase(Locale.ENGLISH));
        if (effectSupplier == null) {
            return null;
        }

        Filter<Object> filter = (Filter<Object>) effectSupplier.apply(configuration);
        return inverted ? new InvertedFilter(filter) : filter;
    }
}
