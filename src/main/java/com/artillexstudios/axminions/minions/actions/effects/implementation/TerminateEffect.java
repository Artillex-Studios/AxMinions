package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axminions.exception.ForcedMinionTickFailException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;

public final class TerminateEffect extends Effect<Object, Object> {

    public TerminateEffect(MapConfigurationGetter configuration) {
        super(configuration);
    }

    @Override
    public Object run(Minion minion, Object argument) {
        throw ForcedMinionTickFailException.INSTANCE;
    }

    @Override
    public Class<Object> inputClass() {
        return Object.class;
    }

    @Override
    public Class<Object> outputClass() {
        return Object.class;
    }
}
