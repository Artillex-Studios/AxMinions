package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public final class BreakEffect extends Effect<Location, ItemCollection> {

    public BreakEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        Collection<ItemStack> items = Integrations.BLOCK.lootAndBreak(argument, minion.tool());
        if (items == null) {
            return null;
        }

        return new ItemCollection(items);
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
