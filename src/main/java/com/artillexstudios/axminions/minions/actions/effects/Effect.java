package com.artillexstudios.axminions.minions.actions.effects;

import com.artillexstudios.axminions.api.events.EffectDispatchEvent;
import com.artillexstudios.axminions.minions.Minion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map;

public abstract class Effect<T, Z> {
    private final Map<Object, Object> configuration;
    private ObjectArrayList<Effect<Z, ?>> children;
    private Effect<?, T> parent;
    // TODO: Requirement, else, etc.

    public Effect(Map<Object, Object> configuration) {
        this.configuration = configuration;

        // TODO: Else, requirements, etc.
    }

    public final void addChildren(Effect<Z, ?> effect) {
        if (this.children == null) {
            this.children = ObjectArrayList.of(effect);
        } else {
            this.children.add(effect);
        }

        effect.parent = this;
    }

    public void dispatch(Minion minion, T argument) {
        Z out = run(minion, argument);

        new EffectDispatchEvent(minion, this, argument).call();
        ObjectArrayList<Effect<Z, ?>> children = this.children;
        if (children == null) {
            return;
        }

        int childrenSize = children.size();
        for (int i = 0; i < childrenSize; i++) {
            Effect<Z, ?> child = children.get(i);
            child.dispatch(minion, out);
        }
    }

    public Map<Object, Object> configuration() {
        return configuration;
    }

    public abstract Z run(Minion minion, T argument);

    public abstract Class<?> inputClass();

    public abstract Class<?> outputClass();

    public final Effect<?, T> parent() {
        return parent;
    }

    public final ObjectArrayList<Effect<Z, ?>> children() {
        return children;
    }
}
