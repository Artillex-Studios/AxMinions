package com.artillexstudios.axminions.minions.actions.collectors.options;

import java.util.Objects;

public class CollectorOption<T> {
    private final String id;

    public CollectorOption(String id) {
        this.id = id;
    }

    public String identifier() {
        return this.id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof CollectorOption<?> that)) {
            return false;
        }

        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return "CollectorOption{" +
                "id='" + this.id + '\'' +
                '}';
    }
}
