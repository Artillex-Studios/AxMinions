package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.RequirementOptionNotPresentException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.EffectCompiler;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOptions;
import com.artillexstudios.axminions.minions.actions.collectors.options.parser.CollectorOptionRegistry;
import com.artillexstudios.axminions.minions.actions.collectors.options.parser.exception.InvalidCollectorOptionException;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import com.artillexstudios.axminions.minions.actions.requirements.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Collector<T> {
    protected final CollectorContext context;
    protected final List<Requirement> requirements;

    public static Collector<?> of(Map<Object, Object> config, EffectCompiler compiler) {
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

        List<Requirement> parsedRequirements = new ArrayList<>();
        List<Map<Object, Object>> requirements = (List<Map<Object, Object>>) config.get("requirements");
        if (requirements != null) {
            for (Map<Object, Object> requirementConfig : requirements) {
                String requirementId = (String) requirementConfig.get("id");
                if (requirementId == null) {
                    LogUtils.warn("Requirement id is not present for collector id {}!", collectorID);
                    continue;
                }

                List<Effect<Object, Object>> elseEffects = null;
                List<Map<Object, Object>> elseBranch = (List<Map<Object, Object>>) requirementConfig.get("else");
                if (elseBranch != null) {
                    elseEffects = compiler.compile(null, null, elseBranch);
                }

                Requirement requirement;
                try {
                    requirement = Requirements.parse(requirementId, requirementConfig, elseEffects);
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

        CollectorContext.Builder contextBuilder = CollectorContext.builder()
                .withOption(CollectorOptions.COLLECTOR_ID, collectorID);

        try {
            CollectorOptionRegistry.parseAll(config, contextBuilder);
        } catch (InvalidCollectorOptionException exception) {
            return null;
        }

        return CollectorRegistry.get(collectorID, contextBuilder, parsedRequirements);
    }

    public Collector(CollectorContext context, List<Requirement> requirements) {
        this.context = context;
        this.requirements = requirements;
    }

    public boolean areRequirementsMet(Minion minion) {
        for (Requirement requirement : this.requirements) {
            if (!requirement.check(minion)) {
                requirement.dispatchElse(minion);
                return false;
            }
        }

        return true;
    }

    public abstract Class<?> getCollectedClass();

    public abstract void collect(Minion minion, Consumer<T> consumer);
}
