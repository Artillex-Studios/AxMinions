package com.artillexstudios.axminions.minions.actions.effects;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.api.events.EffectDispatchEvent;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map;

public abstract class Effect<T, Z> {
    private final Map<Object, Object> configuration;
    private ObjectArrayList<Effect<Z, ?>> children;
    private ObjectArrayList<Effect<T, ?>> elseBranch;
    private ObjectArrayList<Requirement> requirements;
    private Effect<?, ?> parent;

    public Effect(Map<Object, Object> configuration) {
        this.configuration = configuration;
    }

    public final void addChildren(Effect<Z, ?> effect) {
        if (this.children == null) {
            this.children = ObjectArrayList.of(effect);
        } else {
            this.children.add(effect);
        }

        effect.parent = this;
    }

    public final void addElseBranch(Effect<T, ?> effect) {
        if (this.elseBranch == null) {
            this.elseBranch = ObjectArrayList.of(effect);
        } else {
            this.elseBranch.add(effect);
        }
    }

    public final void addRequirement(Requirement requirement) {
        if (this.requirements == null) {
            this.requirements = ObjectArrayList.of(requirement);
        } else {
            this.requirements.add(requirement);
        }
    }

    public void dispatch(Minion minion, T argument) {
        new EffectDispatchEvent(minion, this, argument).call();
        if (!this.areRequirementsMet(minion)) {
            for (Effect<T, ?> branch : this.elseBranch) {
                branch.dispatch(minion, argument);
            }
            return;
        }

        if (!this.validate(argument)) {
            LogUtils.warn("Failed to dispatch effect for minion {} due to an invalid argument: {}!", minion, argument == null ? "absolutely nothing, none, null, nothing, 0" : argument);
            return;
        }

        Z out = this.run(minion, argument);

        if (out == null) {
            return;
        }

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

    public boolean areRequirementsMet(Minion minion) {
        ObjectArrayList<Requirement> requirements = this.requirements;
        if (requirements == null) {
            return true;
        }

        int requirementsSize = requirements.size();

        if (requirementsSize == 0) {
            return true;
        }

        for (int i = 0; i < requirementsSize; i++) {
            Requirement requirement = requirements.get(i);
            if (!requirement.check(minion)) {
                requirement.dispatchElse(minion);
                return false;
            }
        }

        return true;
    }

    public boolean validate(T input) {
        return true;
    }

    public Map<Object, Object> configuration() {
        return this.configuration;
    }

    public abstract Z run(Minion minion, T argument);

    public abstract Class<T> inputClass();

    public abstract Class<Z> outputClass();

    public final Effect<?, ?> parent() {
        return this.parent;
    }

    public final ObjectArrayList<Effect<Z, ?>> children() {
        return this.children;
    }
}
