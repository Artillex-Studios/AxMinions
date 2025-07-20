package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Filters;
import com.artillexstudios.axminions.utils.ItemCollection;
import com.artillexstudios.axminions.utils.MaterialMatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class BreakEffect extends Effect<Location, ItemCollection> {
    private final Function<Material, Material> mapper;

    public BreakEffect(Map<Object, Object> configuration) {
        super(configuration);
        List<Map<Object, Object>> options = (List<Map<Object, Object>>) configuration.get("options");
        if (options != null) {
            final List<Pair<List<Filter<?>>, List<Material>>> replacements = new ArrayList<>();
            for (Map<Object, Object> option : options) {
                List<Map<Object, Object>> filterSection = (List<Map<Object, Object>>) option.get("filters");
                if (filterSection == null) {
                    replacements.add(Pair.of(List.of(), null));
                } else {
                    List<Filter<?>> filters = new ArrayList<>(filterSection.size());
                    for (Map<Object, Object> filter : filterSection) {
                        filters.add(Filters.parse(filter));
                    }
                    List<String> replaceSection = (List<String>) option.get("replacements");
                    if (replaceSection != null) {
                        replacements.add(Pair.of(filters, MaterialMatcher.matchAll(replaceSection)));
                    } else {
                        LogUtils.warn("Could not add replacement options due to missing replacements section!");
                    }
                }
            }

            this.mapper = m -> {
                outer:
                for (Pair<List<Filter<?>>, List<Material>> replacement : replacements) {
                    List<Filter<?>> filters = replacement.first();
                    for (Filter<?> filter : filters) {
                        if (!filter.isAllowed(m)) {
                            continue outer;
                        }
                    }

                    if (replacement.second().size() > 1) {
                        return replacement.second().get(ThreadLocalRandom.current().nextInt(replacement.second().size()));
                    }

                    return replacement.second().getFirst();
                }

                return Material.AIR;
            };
        } else {
            this.mapper = m -> Material.AIR;
        }
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        if (Config.debug) {
            LogUtils.debug("Break effect run!");
        }
        Collection<ItemStack> items = Integrations.BLOCK.lootAndBreak(argument, minion.tool(), this.mapper);
        if (items == null) {
            if (Config.debug) {
                LogUtils.debug("Items null!");
            }
            return null;
        }

        return new ItemCollection(items);
    }

    @Override
    public boolean validate(Location input) {
        return input != null;
    }

    @Override
    public Class<?> inputClass() {
        return Location.class;
    }

    @Override
    public Class<?> outputClass() {
        return ItemCollection.class;
    }
}
