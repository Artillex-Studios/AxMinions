package com.artillexstudios.axminions.minions.actions.filters;

import com.artillexstudios.axminions.exception.TransformerNotPresentException;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

public abstract class Filter<T> {
    private final IdentityHashMap<Class<?>, Transformer<?, ?>> transformers = new IdentityHashMap<>();
    private final Collection<Transformer<?, ?>> unmodifiableTransformers = Collections.unmodifiableCollection(transformers.values());

    public abstract boolean isAllowed(Object object);

    public abstract List<Class<?>> inputClasses();

    public void addTransformer(Class<?> clazz, Transformer<?, ?> transformer) {
        this.transformers.put(clazz, transformer);
    }

    public <Z> Transformer<Z, T> transformer(Class<Z> clazz) {
        Transformer<Z, T> transformer = (Transformer<Z, T>) transformers.get(clazz);
        if (transformer == null) {
            throw new TransformerNotPresentException();
        }

        return transformer;
    }

    public Collection<Transformer<?, ?>> transformers() {
        return this.unmodifiableTransformers;
    }
}
