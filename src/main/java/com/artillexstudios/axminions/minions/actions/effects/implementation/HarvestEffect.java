package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.Map;

// TODO: Get loot, set type/age based on type
public final class HarvestEffect extends Effect<Location, ItemCollection> {

    public HarvestEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        BlockData data = argument.getBlock().getBlockData();

        if (data instanceof Ageable ageable) {
            ageable.setAge(0);
        }
        return null;
    }

    @Override
    public Class<Location> inputClass() {
        return Location.class;
    }

    @Override
    public Class<ItemCollection> outputClass() {
        return ItemCollection.class;
    }
}
