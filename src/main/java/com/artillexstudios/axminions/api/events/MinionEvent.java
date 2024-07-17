package com.artillexstudios.axminions.api.events;

import com.artillexstudios.axminions.minions.Minion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class MinionEvent extends Event {
    private final Minion minion;

    public MinionEvent(Minion minion) {
        this.minion = minion;
    }

    public Minion minion() {
        return minion;
    }

    public boolean call() {
        if (this instanceof Cancellable cancellable) {
            return cancellable.isCancelled();
        }

        return true;
    }
}
