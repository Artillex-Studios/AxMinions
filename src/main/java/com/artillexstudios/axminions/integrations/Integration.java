package com.artillexstudios.axminions.integrations;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;

public abstract class Integration<T extends Integrable> {
    private final ObjectArrayList<T> integrations = new ObjectArrayList<>();
    private final List<T> view = Collections.unmodifiableList(integrations);

    public void register(T integration) {
        this.integrations.add(integration);
    }

    public List<T> integrations() {
        return this.view;
    }
}
