package com.artillexstudios.axminions.minions.actions;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.RequirementOptionNotPresentException;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.effects.Effects;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import com.artillexstudios.axminions.minions.actions.requirements.Requirements;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public final class BasicEffectCompiler implements EffectCompiler {

    @Override
    public List<Effect<Object, Object>> compile(Collector<?> collector, Effect<Object, Object> parent, List<Map<Object, Object>> list) {
        ObjectArrayList<Effect<Object, Object>> effects = new ObjectArrayList<>(2);
        if (Config.debug) {
            LogUtils.debug("CompileEffects");
        }
        if (list == null) {
            if (Config.debug) {
                LogUtils.debug("List is null!");
            }
            return List.of();
        }

        for (Map<Object, Object> map : list) {
            String id = (String) map.get("id");
            if (Config.debug) {
                LogUtils.debug("id {}", id == null ? "null" : id);
            }
            if (id == null) {
                continue;
            }

            Effect<Object, Object> effect = Effects.parse(id, map);
            if (effect == null) {
                continue;
            }

            if (parent == null) {
                if (collector != null) {
                    if (collector.getCollectedClass() != effect.inputClass() && collector.getCollectedClass().isAssignableFrom(effect.inputClass())) {
                        LogUtils.warn("Collector {} is incompatible with effect {}. Expected collector to collect: {} but found: {}", collector.getClass().getName(), effect.getClass().getName(), effect.inputClass().getName(), collector.getCollectedClass().getName());
                        continue;
                    }
                }
            } else {
                if (parent.outputClass() != effect.inputClass() && parent.outputClass().isAssignableFrom(effect.inputClass())) {
                    LogUtils.warn("Effect {} is incompatible with effect {}. Expected effect to return: {} but found: {}", parent.getClass().getName(), effect.getClass().getName(), effect.inputClass().getName(), parent.outputClass().getName());
                    continue;
                }
            }

            effects.add(effect);

            List<Map<Object, Object>> requirements = (List<Map<Object, Object>>) map.get("requirements");
            if (requirements != null) {
                for (Map<Object, Object> requirementConfig : requirements) {
                    String requirementId = (String) requirementConfig.get("id");
                    if (requirementId == null) {
                        LogUtils.warn("Requirement id is not present for effect id {}!", id);
                        continue;
                    }

                    List<Effect<Object, Object>> elseEffects = null;
                    List<Map<Object, Object>> elseBranch = (List<Map<Object, Object>>) requirementConfig.get("else");
                    if (elseBranch != null) {
                        elseEffects = this.compile(collector, null, elseBranch);
                    }

                    Requirement requirement;
                    try {
                        requirement = Requirements.parse(requirementId, requirementConfig, elseEffects);
                    } catch (RequirementOptionNotPresentException exception) {
                        LogUtils.warn("The requirement provided is missing an option with key {}!", exception.option());
                        continue;
                    }

                    if (requirement == null) {
                        LogUtils.warn("Could not find requirement with id {} for effect {}!", requirementId, id);
                        continue;
                    }

                    if (Config.debug) {
                        LogUtils.debug("Adding requirement {}", requirement);
                    }
                    effect.addRequirement(requirement);
                }
            }

            Map<Object, Object> elseBranch = (Map<Object, Object>) map.get("else");
            if (elseBranch != null) {
                List<Map<Object, Object>> elseEffects = (List<Map<Object, Object>>) elseBranch.get("effects");
                if (requirements != null) {
                    if (elseEffects != null) {
                        for (Effect<Object, Object> children : this.compile(collector, effect, elseEffects)) {
                            effect.addElseBranch(children);
                        }
                    } else {
                        LogUtils.warn("No effects defined for else of effect {}!", id);
                    }
                } else {
                    LogUtils.warn("Effect {} has no requirements, but has else branch!", id);
                }
            }

            List<Map<Object, Object>> childEffects = (List<Map<Object, Object>>) map.get("effects");
            if (Config.debug) {
                LogUtils.debug("Child {}", childEffects);
            }
            if (childEffects == null) {
                continue;
            }

            for (Effect<Object, Object> children : this.compile(collector, effect, childEffects)) {
                effect.addChildren(children);
            }
        }

        return effects;
    }
}
