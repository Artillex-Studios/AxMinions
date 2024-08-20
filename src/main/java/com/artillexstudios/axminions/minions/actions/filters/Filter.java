package com.artillexstudios.axminions.minions.actions.filters;

import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Filter<T> {
    private final Object2ObjectArrayMap<Class<?>, Transformer<?, ?>> transformers = new Object2ObjectArrayMap<>();
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
