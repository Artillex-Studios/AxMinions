package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.Location;

import java.util.Map;

public class BreakEffect extends Effect<Location, ItemCollection> {

    public BreakEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        return new ItemCollection(Integrations.BLOCK.lootAndBreak(argument, minion.tool()));
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
