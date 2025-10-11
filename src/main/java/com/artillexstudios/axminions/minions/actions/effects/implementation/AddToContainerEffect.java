package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.integrations.Integrations;
import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class AddToContainerEffect extends Effect<ItemCollection, ItemCollection> {

    public AddToContainerEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, ItemCollection argument) {
        if (minion.linkedChest() == null) {
            return argument;
        }

        Integrations.STORAGE.push(minion.linkedChest(), argument.items());
        return null;
    }


    @Override
    public Class<ItemCollection> inputClass() {
        return ItemCollection.class;
    }

    @Override
    public Class<ItemCollection> outputClass() {
        return ItemCollection.class;
    }
}
