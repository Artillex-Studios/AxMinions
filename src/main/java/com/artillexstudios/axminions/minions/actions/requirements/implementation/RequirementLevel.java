package com.artillexstudios.axminions.minions.actions.requirements.implementation;

import com.artillexstudios.axapi.config.adapters.MapConfigurationGetter;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

import java.util.List;

public final class RequirementLevel extends Requirement {
    private final CompiledExpression expression;

    public RequirementLevel(MapConfigurationGetter parameters, List<Effect<Object, Object>> elseEffects) {
        super(parameters, elseEffects);

        this.expression = Crunch.compileExpression(this.get("level"));
    }

    @Override
    public boolean check(Minion minion) {
        return minion.level().id() >= expression.evaluate();
    }
}
