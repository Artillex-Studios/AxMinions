package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axminions.minions.actions.filters.Filter;

import java.util.List;

public final class InvertedFilter extends Filter<Object> {
    private final Filter<Object> filter;

    public InvertedFilter(Filter<Object> filter) {
        this.filter = filter;
    }

    @Override
    public boolean isAllowed(Object object) {
        return !this.filter.isAllowed(object);
    }

    @Override
    public List<Class<?>> inputClasses() {
        return this.filter.inputClasses();
    }
}
