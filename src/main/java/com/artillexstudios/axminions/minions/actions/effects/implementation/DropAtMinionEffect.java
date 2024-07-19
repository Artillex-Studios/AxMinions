package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class DropAtMinionEffect extends Effect<ItemCollection, ItemCollection> {

    public DropAtMinionEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, ItemCollection argument) {
        World world = minion.location().getWorld();
        if (world == null) {
            return ItemCollection.EMPTY;
        }

        for (ItemStack itemStack : argument.items()) {
            world.dropItem(minion.location(), itemStack);
        }

        return ItemCollection.EMPTY;
    }

    @Override
    public Class<?> inputClass() {
        return ItemCollection.class;
    }

    @Override
    public Class<?> outputClass() {
        return ItemCollection.class;
    }
}
