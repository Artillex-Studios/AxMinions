package com.artillexstudios.axminions.minions.actions.filters;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;

public abstract class Filter<T> {
    private final ObjectArrayList<Transformer<?, ?>> transformers = new ObjectArrayList<>();
    private final List<Transformer<?, ?>> unmodifiableTransformers = Collections.unmodifiableList(transformers);

    public abstract boolean isAllowed(T object);

    public abstract Class<?> inputClass();

    public void addTransformer(Transformer<?, ?> transformer) {
        this.transformers.add(transformer);
    }

    public List<Transformer<?, ?>> transformers() {
        return this.unmodifiableTransformers;
    }
}
