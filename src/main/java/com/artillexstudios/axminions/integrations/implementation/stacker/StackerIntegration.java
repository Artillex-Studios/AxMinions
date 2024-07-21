package com.artillexstudios.axminions.integrations.implementation.stacker;

import com.artillexstudios.axminions.integrations.Integration;
import org.bukkit.entity.Entity;

import java.util.OptionalLong;

public final class StackerIntegration extends Integration<StackerIntegrable> {

    public StackerIntegration() {
        this.register(new DefaultStackerIntegrable());
    }

    public long getStackSize(Entity entity) {
        for (StackerIntegrable integration : this.integrations()) {
            OptionalLong stackSize = integration.stackSize(entity);
            if (stackSize.isPresent()) {
                return stackSize.getAsLong();
            }
        }

        return 1;
    }
}
