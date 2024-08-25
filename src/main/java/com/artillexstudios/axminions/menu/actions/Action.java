package com.artillexstudios.axminions.menu.actions;

import com.artillexstudios.axminions.minions.Minion;
import org.bukkit.entity.Player;

public abstract class Action {
    private final String id;

    public Action(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public abstract void run(Player player, Minion minion, String arguments);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Action action)) {
            return false;
        }

        return this.getId().equals(action.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
