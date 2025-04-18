package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOption;

import java.util.HashMap;
import java.util.Objects;

public final class CollectorContext {
    private final HashMap<CollectorOption<?>, Object> options;

    private CollectorContext(HashMap<CollectorOption<?>, Object> options) {
        this.options = options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> T option(CollectorOption<T> option) {
        return (T) this.options.get(option);
    }

    public <T> T optionOrDefault(CollectorOption<T> option, T def) {
        T found = this.option(option);
        return found == null ? def : found;
    }

    public <T> T optionOrThrow(CollectorOption<T> option) throws CollectorOptionNotPresentException {
        T found = this.option(option);
        if (found == null) {
            throw new CollectorOptionNotPresentException(option.identifier());
        }

        return found;
    }

    public CollectorContext copy() {
        return new CollectorContext(new HashMap<>(this.options));
    }

    public CollectorContext.Builder toBuilder() {
        CollectorContext.Builder builder = builder();
        this.options.forEach((key, value) -> builder.withOption((CollectorOption<Object>) key, value));
        return builder;
    }

    public <T> boolean contains(CollectorOption<T> option) {
        return this.options.containsKey(option);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectorContext that)) {
            return false;
        }

        return Objects.equals(this.options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.options);
    }

    @Override
    public String toString() {
        return "CollectorContext{" +
                "options=" + this.options +
                '}';
    }

    public static final class Builder {
        private final HashMap<CollectorOption<?>, Object> options = new HashMap<>();

        public <T> Builder withOption(CollectorOption<T> option, T value) {
            this.options.put(option, value);
            return this;
        }

        public <T> T option(CollectorOption<T> option) {
            return (T) this.options.get(option);
        }

        public CollectorContext build() {
            return new CollectorContext(this.options);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "options=" + this.options +
                    '}';
        }
    }
}
