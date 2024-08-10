package com.artillexstudios.axminions.minions.actions.requirements;

import com.artillexstudios.axminions.minions.Minion;

import java.util.Map;

public abstract class Requirement {
    private final Map<Object, Object> parameters;

    public Requirement(Map<Object, Object> parameters) {
        this.parameters = parameters;
    }

    public abstract boolean check(Minion minion);

    public <T> T get(String key) {
        return (T) this.parameters.get(key);
    }

    public <T> T getOrDefault(String key, T def) {
        return (T) this.parameters.getOrDefault(key, def);
    }
}
