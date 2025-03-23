package com.artillexstudios.axminions.minions.actions;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.ForcedMinionTickFailException;
import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.collectors.Collector;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public final class CompiledAction {
    private final Collector<?> collector;
    private final List<Effect<Object, Object>> effects;

    public CompiledAction(EffectCompiler compiler, Map<Object, Object> map) {
        this.collector = Collector.of((Map<Object, Object>) map.get("collector"), compiler);
        this.effects = new ObjectArrayList<>();
        this.effects.addAll(compiler.compile(this.collector, null, (List<Map<Object, Object>>) map.get("effects")));

        if (Config.debug) {
            LogUtils.debug("Printing children of compiledaction!");

            for (Effect<Object, Object> effect : this.effects) {
                printChildren(effect);
            }
        }
    }

    // TODO: Add support for custom compilers
    public static CompiledAction of(Map<Object, Object> map) {
        return new CompiledAction(new BasicEffectCompiler(), map);
    }

    private void printChildren(Effect<?, ?> effect) {
        if (effect.children() == null || effect.children().isEmpty()) {
            LogUtils.debug("Has no children");
            return;
        }

        for (Effect<?, ?> child : effect.children()) {
            LogUtils.debug("Parent: {} Children: {}", effect, child);
            this.printChildren(child);
        }
    }

    public void run(Minion minion) {
        // TODO: Optimize: can we somehow batch actions/lazily evaluate instead of eager evaluation
        // We want this collector to avoid the initialization of a list. I'd think that
        // this is cheaper than a whole array/list initialization.
        LogUtils.debug("Are requirements met check {}", minion.type().name());
        if (!this.collector.areRequirementsMet(minion)) {
            LogUtils.debug("Not met");
            return;
        }

        LogUtils.debug("Met");
        this.collector.collect(minion, collected -> {
            for (Effect<Object, Object> effect : this.effects) {
                try {
                    effect.dispatch(minion, collected);
                } catch (ForcedMinionTickFailException exception) {
                    throw exception;
                } catch (MinionTickFailException exception) {
                    LogUtils.warn("An unexpected error occurred while ticking minion {} at {}!", minion.type().name(), minion.location(), exception);
                    throw exception;
                }
            }
        });
    }
}
