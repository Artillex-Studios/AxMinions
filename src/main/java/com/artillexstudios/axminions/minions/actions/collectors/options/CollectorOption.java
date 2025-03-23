package com.artillexstudios.axminions.minions.actions.collectors.options;

public class CollectorOption<T> {
    private final String id;

    public CollectorOption(String id) {
        this.id = id;
    }

    public String identifier() {
        return this.id;
    }
}
