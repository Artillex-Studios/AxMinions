package com.artillexstudios.axminions.integrations.implementation.stacker;

import org.bukkit.entity.Entity;

import java.util.OptionalLong;

public final class DefaultStackerIntegrable implements StackerIntegrable {

    @Override
    public OptionalLong stackSize(Entity entity) {
        return OptionalLong.empty();
    }

    @Override
    public void stackSize(Entity entity, long amount) {

    }
}
