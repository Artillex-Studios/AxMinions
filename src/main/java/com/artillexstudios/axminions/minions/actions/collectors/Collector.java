package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.RequirementOptionNotPresentException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.implementation.CollectEffect;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Filters;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import com.artillexstudios.axminions.minions.actions.requirements.Requirements;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Collector<T> {
    protected final CollectorShape shape;
    protected final CompiledExpression expression;
    protected final List<Filter<?>> filters;
    protected final List<Requirement> requirements;

    public static Collector<?> of(Map<Object, Object> config) {
        if (Config.debug) {
            LogUtils.debug("Collector of {}", config);
        }
        if (config == null) {
            return null;
        }

        String collectorID = (String) config.get("id");
        if (collectorID == null) {
            LogUtils.warn("Collector id was not defined!");
            return null;
        }

        if (!CollectorRegistry.exists(collectorID)) {
            LogUtils.warn("Collector id {} not present! Collector ids: {}", collectorID, CollectorRegistry.keys());
            return null;
        }

        String shape = (String) config.get("shape");
        if (shape == null) {
            LogUtils.warn("Shape was not defined!");
            return null;
        }

        Optional<CollectorShape> collectorShape = CollectorShape.parse(shape);
        if (collectorShape.isEmpty()) {
            LogUtils.warn("Invalid shape! Shapes: {}", Arrays.toString(CollectorShape.entries));
            return null;
        }

        String radius = (String) config.get("radius");
        if (radius == null) {
            LogUtils.warn("Radius was not defined!");
            return null;
        }

        List<Requirement> parsedRequirements = new ArrayList<>();
        List<Map<Object, Object>> requirements = (List<Map<Object, Object>>) config.get("requirements");
        if (requirements != null) {
            for (Map<Object, Object> requirementConfig : requirements) {
                String requirementId = (String) requirementConfig.get("id");
                if (requirementId == null) {
                    LogUtils.warn("Requirement id is not present for collector id {}!", collectorID);
                    continue;
                }

                Requirement requirement;
                try {
                    requirement = Requirements.parse(requirementId, requirementConfig);
                } catch (RequirementOptionNotPresentException exception) {
                    LogUtils.warn("The requirement provided is missing an option with key {}!", exception.option());
                    continue;
                }

                if (requirement == null) {
                    LogUtils.warn("Could not find requirement with id {} for collector {}!", requirementId, collectorID);
                    continue;
                }

                if (Config.debug) {
                    LogUtils.debug("Adding requirement {}", requirement);
                }

                parsedRequirements.add(requirement);
            }
        }

        List<Map<Object, Object>> filterMap = (List<Map<Object, Object>>) config.get("filters");
        List<Filter<?>> filters = new ObjectArrayList<>(1);
        if (filterMap != null) {
            for (Map<Object, Object> map : filterMap) {
                String filterId = (String) map.get("id");

                if (filterId == null) {
                    LogUtils.warn("Could not find id in filter config!");
                    continue;
                }

                Filter<?> filter = Filters.parse(filterId, map);
                if (filter == null) {
                    LogUtils.warn("Could not find filter with id {}!", filterId);
                    continue;
                }

                List<Class<?>> inputClasses = filter.inputClasses();
                Class<?> collectedClass = CollectorRegistry.getCollectedClass(collectorID);
                if (!inputClasses.contains(collectedClass)) {
                    LogUtils.error("Could not apply filter with id {} to collector {} due to mismatching input! Filter input: {}, Collector output: {}.", filterId, collectorID, String.join(", ", inputClasses.stream().map(Class::getName).toList()), collectedClass.getName());
                    continue;
                }

                filters.add(filter);
            }
        }

        return CollectorRegistry.get(collectorID, collectorShape.get(), Crunch.compileExpression(radius.replace("<level>", "$1")), filters, parsedRequirements);
    }

    public Collector(CollectorShape shape, CompiledExpression expression, List<Filter<?>> filters, List<Requirement> requirements) {
        this.shape = shape;
        this.expression = expression;
        this.filters = filters;
        this.requirements = requirements;
    }

    public boolean areRequirementsMet(Minion minion) {
        for (Requirement requirement : this.requirements) {
            if (!requirement.check(minion)) {
                return false;
            }
        }

        return true;
    }

    public abstract Class<?> getCollectedClass();

    public abstract void collect(Minion minion, Consumer<T> consumer);
}
