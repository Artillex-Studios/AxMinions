package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.exception.MinionTickFailException;
import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class DropAtMinionEffect extends Effect<ItemCollection, ItemCollection> {

    public DropAtMinionEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, ItemCollection argument) {
        if (argument == ItemCollection.EMPTY) {
            return null;
        }

        World world = minion.location().getWorld();
        if (world == null) {
            throw new MinionTickFailException("World is null!");
        }

        Integrations.STORAGE.pushDrop(minion.location(), argument.items());
        return null;
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
