package com.artillexstudios.axminions.minions.actions.requirements;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.exception.ForcedMinionTickFailException;
import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.exception.RequirementOptionNotPresentException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

import java.util.List;
import java.util.Map;

public abstract class Requirement {
    private final Map<Object, Object> parameters;
    private final List<Effect<Object, Object>> elseEffects;

    public Requirement(Map<Object, Object> parameters, List<Effect<Object, Object>> elseEffects) {
        this.parameters = parameters;
        this.elseEffects = elseEffects;
    }

    public abstract boolean check(Minion minion);

    public <T> T get(String key) {
        T param = (T) this.parameters.get(key);
        if (param == null) {
            throw new RequirementOptionNotPresentException(key);
        }

        return param;
    }

    public <T> T getOrDefault(String key, T def) {
        return (T) this.parameters.getOrDefault(key, def);
    }

    public List<Effect<Object, Object>> elseEffects() {
        return this.elseEffects;
    }

    public void dispatchElse(Minion minion) {
        if (Config.debug) {
            LogUtils.debug("DispatchElse");
        }
        if (this.elseEffects == null) {
            if (Config.debug) {
                LogUtils.debug("No else effects");
            }
            return;
        }

        for (Effect<Object, Object> effect : this.elseEffects) {
            try {
                effect.dispatch(minion, null);
            } catch (ForcedMinionTickFailException exception) {
                throw exception;
            } catch (MinionTickFailException exception) {
                LogUtils.warn("An unexpected error occurred while ticking minion {} at {}!", minion.type().name(), minion.location(), exception);
                throw exception;
            }
        }
    }
}
