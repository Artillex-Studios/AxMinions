package com.artillexstudios.axminions.minions.actions.collectors;

import com.artillexstudios.axapi.collections.IdentityArrayMap;
import com.artillexstudios.axminions.minions.actions.collectors.options.CollectorOption;

public final class CollectorContext {
    private final IdentityArrayMap<CollectorOption<?>, Object> options;

    private CollectorContext(IdentityArrayMap<CollectorOption<?>, Object> options) {
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
        IdentityArrayMap<CollectorOption<?>, Object> options = new IdentityArrayMap<>();
        this.options.forEach(options::put);
        return new CollectorContext(options);
    }

    public CollectorContext.Builder toBuilder() {
        CollectorContext.Builder builder = builder();
        this.options.forEach((key, value) -> builder.withOption((CollectorOption<Object>) key, value));
        return builder;
    }

    public <T> boolean contains(CollectorOption<T> option) {
        return this.options.containsKey(option);
    }

    public static final class Builder {
        private final IdentityArrayMap<CollectorOption<?>, Object> options = new IdentityArrayMap<>();

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
    }
}
