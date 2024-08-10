package com.artillexstudios.axminions.integrations;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Math;

import java.util.Collections;
import java.util.List;

public abstract class Integration<T extends Integrable> {
    private final ObjectArrayList<T> integrations = new ObjectArrayList<>();
    private final List<T> view = Collections.unmodifiableList(integrations);

    public Integration() {
        this.reload();
    }

    public void register(T integration) {
        this.integrations.add(integration);
    }

    public void register(int priority, T integration) {
        this.integrations.add(Math.clamp(0, integrations.size(), priority), integration);
    }

    public void reload() {
        this.integrations.clear();

        this.reload0();
    }

    public abstract void reload0();

    public List<T> integrations() {
        return this.view;
    }
}
