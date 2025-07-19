package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axminions.exception.ForcedMinionTickFailException;
import com.artillexstudios.axminions.exception.MinionWarningException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import net.kyori.adventure.text.Component;

import java.util.Map;

public final class WarningEffect extends Effect<Object, Object> {
    private final Component message;

    public WarningEffect(Map<Object, Object> configuration) {
        super(configuration);
        this.message = StringUtils.format((String) this.configuration().get("message"));
    }

    @Override
    public Object run(Minion minion, Object argument) {
        minion.warnings().show(this.message);
        throw MinionWarningException.INSTANCE;
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
