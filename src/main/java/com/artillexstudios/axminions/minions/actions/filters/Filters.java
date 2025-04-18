package com.artillexstudios.axminions.minions.actions.filters;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.minions.actions.filters.implementation.AnimalFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.EntityTypeFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.InvertedFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.MaterialFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.StoneGeneratorFilter;
import com.artillexstudios.axminions.minions.actions.filters.implementation.TamedFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class Filters {
    public static final Filter<Object> EMPTY = new Filter<>() {
        @Override
        public boolean isAllowed(Object object) {
            return true;
        }
    };
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

    public static Filter<Object> parse(Map<Object, Object> configuration) {
        String id = (String) configuration.get("id");

        if (id == null) {
            LogUtils.warn("Could not find id in filter config!");
            return null;
        }

        boolean inverted = false;
        if (id.startsWith("!")) {
            inverted = true;
        }

        id = StringUtils.replaceOnce(id, "!", "");

        Function<Map<Object, Object>, Filter<?>> effectSupplier = filters.get(id.toLowerCase(Locale.ENGLISH));
        if (effectSupplier == null) {
            return Filters.EMPTY;
        }

        Filter<Object> filter = (Filter<Object>) effectSupplier.apply(configuration);
        return inverted ? new InvertedFilter(filter) : filter;
    }
}
