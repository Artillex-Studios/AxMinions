package com.artillexstudios.axminions.minions.actions.effects;

import com.artillexstudios.axminions.minions.Minion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map;

public abstract class Effect<T, Z> {
    private final Map<Object, Object> configuration;
    private final ObjectArrayList<Effect<Z, ?>> children = new ObjectArrayList<>();
    private Effect<?, T> parent;
    // TODO: Requirement, else, etc.

    public Effect(Map<Object, Object> configuration) {
        this.configuration = configuration;

        // TODO: Else, requirements, etc.
    }

    public final void addChildren(Effect<Z, ?> effect) {
        this.children.add(effect);
        effect.parent = this;
    }

    public void dispatch(Minion minion, T argument) {
        Z out = run(minion, argument);
        // TODO: Effect dispatch event
        for (Effect<Z, ?> child : children) {
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

    public ObjectArrayList<Effect<Z, ?>> children() {
        return children;
    }
}
