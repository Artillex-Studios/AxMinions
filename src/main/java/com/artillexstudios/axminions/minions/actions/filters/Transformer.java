package com.artillexstudios.axminions.minions.actions.filters;

public interface Transformer<T, Z> {

    Z transform(T object);

    Class<?> inputClass();

    Class<?> outputClass();
}
