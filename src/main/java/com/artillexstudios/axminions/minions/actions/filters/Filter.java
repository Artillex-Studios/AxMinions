package com.artillexstudios.axminions.minions.actions.filters;

public abstract class Filter<T> {

    public abstract boolean isAllowed(T object);
}
