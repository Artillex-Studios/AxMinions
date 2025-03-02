package com.artillexstudios.axminions.minions.actions.requirements.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.requirements.Requirement;

import java.util.Map;

public final class RequirementContainer extends Requirement {

    public RequirementContainer(Map<Object, Object> parameters) {
        super(parameters);
    }

    @Override
    public boolean check(Minion minion) {
        return minion.linkedChest() == null;
    }
}
