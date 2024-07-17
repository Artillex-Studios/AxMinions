package com.artillexstudios.axminions.minions.actions;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.effects.Effects;
import com.artillexstudios.axminions.utils.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public final class CompiledAction {
    private final Collector<?> collector;
    private final List<Effect<Object, Object>> effects;

    public CompiledAction(Map<Object, Object> map) {
        this.collector = Collector.of((Map<Object, Object>) map.get("collector"));
        this.effects = new ObjectArrayList<>();
        this.effects.addAll(this.compileEffects(null, (List<Map<Object, Object>>) map.get("effects")));

        LogUtils.debug("Printing!");
        for (Effect<Object, Object> effect : this.effects) {
            printChildren(effect);
        }
    }

    public static CompiledAction of(Map<Object, Object> map) {
        return new CompiledAction(map);
    }

    private void printChildren(Effect<?, ?> effect) {
        if (effect.children().isEmpty()) {
            LogUtils.debug("Has no children");
            return;
        }

        for (Effect<?, ?> child : effect.children()) {
            LogUtils.debug("Parent: {} Children: {}", effect, child);
            printChildren(child);
        }
    }

    private List<Effect<Object, Object>> compileEffects(Effect<Object, Object> parent, List<Map<Object, Object>> list) {
        ObjectArrayList<Effect<Object, Object>> effects = new ObjectArrayList<>(2);
        LogUtils.debug("CompileEffects");
        if (list == null) {
            LogUtils.debug("List is null!");
            return List.of();
        }

        for (Map<Object, Object> map : list) {
            String id = (String) map.get("id");
            LogUtils.debug("id {}", id == null ? "null" : id);
            if (id == null) {
                continue;
            }

            Effect<Object, Object> effect = Effects.parse(id, map);
            if (effect == null) {
                continue;
            }

            if (parent == null) {
                if (collector.getCollectedClass() != effect.inputClass()) {
                    LogUtils.warn("Collector {} is incompatible with effect {}. Expected collector to collect: {} but found: {}", collector.getClass().getName(), effect.getClass().getName(), effect.inputClass().getName(), collector.getCollectedClass().getName());
                    continue;
                }
            } else {
                if (parent.outputClass() != effect.inputClass()) {
                    LogUtils.warn("Effect {} is incompatible with effect {}. Expected effect to return: {} but found: {}", parent.getClass().getName(), effect.getClass().getName(), effect.inputClass().getName(), parent.outputClass().getName());
                    continue;
                }
            }

            effects.add(effect);
            List<Map<Object, Object>> childEffects = (List<Map<Object, Object>>) map.get("effects");
            LogUtils.debug("Child {}", childEffects);
            if (childEffects == null) {
                continue;
            }

            for (Effect<Object, Object> children : compileEffects(effect, childEffects)) {
                effect.addChildren(children);
            }
        }

        return effects;
    }

    public boolean run(Minion minion) {
        // TODO: Optimize: can we somehow batch actions/lazily evaluate instead of eager evaluation
        for (Object collected : this.collector.collect(minion)) {
            for (Effect<Object, Object> effect : this.effects) {
                effect.dispatch(minion, collected);
            }
        }
        // TODO: Return false if the actions can't be ran
        return true;
    }
}
