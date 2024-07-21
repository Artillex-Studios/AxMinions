package com.artillexstudios.axminions.integrations.implementation.stacker;

import com.artillexstudios.axminions.integrations.Integrable;
import org.bukkit.entity.Entity;

import java.util.OptionalLong;

public interface StackerIntegrable extends Integrable {

    OptionalLong stackSize(Entity entity);

    void stackSize(Entity entity, long amount);
}
