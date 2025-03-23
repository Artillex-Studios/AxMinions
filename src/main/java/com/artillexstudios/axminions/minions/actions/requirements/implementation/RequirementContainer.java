package com.artillexstudios.axminions.minions.actions.requirements.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;

import java.util.List;
import java.util.Map;

public final class RequirementContainer extends Requirement {

    public RequirementContainer(Map<Object, Object> parameters, List<Effect<Object, Object>> elseEffects) {
        super(parameters, elseEffects);
    }

    @Override
    public boolean check(Minion minion) {
        return minion.linkedChest() != null;
    }
}
