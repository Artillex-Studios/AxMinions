package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.Location;

import java.util.Map;

public final class FishEffect extends Effect<Location, ItemCollection> {

    public FishEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        

        return null;
    }

    @Override
    public boolean validate(Location input) {
        return input != null;
    }

    @Override
    public Class<?> inputClass() {
        return Location.class;
    }

    @Override
    public Class<?> outputClass() {
        return ItemCollection.class;
    }
}
