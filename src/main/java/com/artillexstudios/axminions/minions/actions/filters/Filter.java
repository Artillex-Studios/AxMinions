package com.artillexstudios.axminions.minions.actions.filters;

import com.artillexstudios.axapi.collections.IdentityArrayMap;
import com.artillexstudios.axminions.exception.TransformerNotPresentException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Filter<T> {
    private final IdentityArrayMap<Class<?>, Transformer<?, ?>> transformers = new IdentityArrayMap<>();
    private final Set<Class<?>> keySet = new HashSet<>();

    public abstract boolean isAllowed(Object object);

    public Set<Class<?>> inputClasses() {
        return this.keySet;
    }

    public void addTransformer(Class<?> clazz, Transformer<?, ?> transformer) {
        this.transformers.put(clazz, transformer);
        this.keySet.clear();
        this.keySet.addAll(this.transformers.entrySet().stream().map(Map.Entry::getKey).toList());
    }

    public <Z> Transformer<Z, T> transformer(Class<Z> clazz) {
        Transformer<Z, T> transformer = (Transformer<Z, T>) this.transformers.get(clazz);
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
