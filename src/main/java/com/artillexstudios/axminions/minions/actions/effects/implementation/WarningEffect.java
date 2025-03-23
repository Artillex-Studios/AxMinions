package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.exception.ForcedMinionTickFailException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

import java.util.Map;

public final class WarningEffect extends Effect<Object, Object> {
    private final String message;

    public WarningEffect(Map<Object, Object> configuration) {
        super(configuration);
        this.message = (String) this.configuration().get("message");
    }

    @Override
    public Object run(Minion minion, Object argument) {
        // TODO: Implement
        LogUtils.warn("Warning: {}", this.message);
        throw ForcedMinionTickFailException.INSTANCE;
    }

    @Override
    public Class<?> inputClass() {
        return Object.class;
    }

    @Override
    public Class<?> outputClass() {
        return Object.class;
    }
}
