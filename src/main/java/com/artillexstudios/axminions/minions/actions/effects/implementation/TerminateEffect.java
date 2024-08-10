package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.exception.ForcedMinionTickFailException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

import java.util.Map;

public final class TerminateEffect extends Effect<Object, Object> {

    public TerminateEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public Object run(Minion minion, Object argument) {
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
