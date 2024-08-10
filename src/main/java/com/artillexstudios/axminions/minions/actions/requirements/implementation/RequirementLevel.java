package com.artillexstudios.axminions.minions.actions.requirements.implementation;

import com.artillexstudios.axminions.exception.RequirementOptionNotPresentException;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

import java.util.Map;

public final class RequirementLevel extends Requirement {
    private final CompiledExpression expression;

    public RequirementLevel(Map<Object, Object> parameters) {
        super(parameters);

        String level = this.get("level");
        if (level == null) {
            throw new RequirementOptionNotPresentException("level");
        }

        this.expression = Crunch.compileExpression(level);
    }

    @Override
    public boolean check(Minion minion) {
        return minion.level().id() >= expression.evaluate();
    }
}
