package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

import java.util.Map;

public final class WarningEffect extends Effect<Object, Object> {

    public WarningEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public Object run(Minion minion, Object argument) {

        return null;
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
