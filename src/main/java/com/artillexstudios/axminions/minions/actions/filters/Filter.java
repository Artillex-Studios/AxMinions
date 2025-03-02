package com.artillexstudios.axminions.minions.actions.filters;

import com.artillexstudios.axapi.collections.IdentityArrayMap;
import com.artillexstudios.axminions.exception.TransformerNotPresentException;

import java.util.List;
import java.util.Map;

public abstract class Filter<T> {
    private final IdentityArrayMap<Class<?>, Transformer<?, ?>> transformers = new IdentityArrayMap<>();

    public abstract boolean isAllowed(Object object);

    public abstract List<Class<?>> inputClasses();

    public void addTransformer(Class<?> clazz, Transformer<?, ?> transformer) {
        this.transformers.put(clazz, transformer);
    }

    public <Z> Transformer<Z, T> transformer(Class<Z> clazz) {
        Transformer<Z, T> transformer = (Transformer<Z, T>) transformers.get(clazz);
        if (transformer == null) {
            for (Map.Entry<Class<?>, Transformer<?, ?>> entry : this.transformers.entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    transformer = (Transformer<Z, T>) entry.getValue();
                    break;
                }
            }
        }

        if (transformer == null) {
            throw new TransformerNotPresentException();
        }

        return transformer;
    }
}
