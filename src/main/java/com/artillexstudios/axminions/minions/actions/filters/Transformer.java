package com.artillexstudios.axminions.minions.actions.filters;

public interface Transformer<T, Z> {

    Z transform(Object object);

    Class<?> inputClass();

    Class<?> outputClass();
}
