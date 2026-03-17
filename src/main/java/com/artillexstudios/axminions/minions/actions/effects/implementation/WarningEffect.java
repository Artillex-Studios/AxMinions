package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axminions.exception.MinionWarningException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import net.kyori.adventure.text.Component;

public final class WarningEffect extends Effect<Object, Object> {
    private final Component message;

    public WarningEffect(MapConfigurationGetter configuration) {
        super(configuration);
        this.message = StringUtils.format(this.configuration().getString("message"));
    }

    @Override
    public Object run(Minion minion, Object argument) {
        minion.warnings().show(this.message);
        throw MinionWarningException.INSTANCE;
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
