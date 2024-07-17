package com.artillexstudios.axminions.minions.actions.effects.implementation;

import com.artillexstudios.axminions.minions.Minion;
import com.artillexstudios.axminions.minions.actions.effects.Effect;
import com.artillexstudios.axminions.utils.ItemCollection;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public class BreakEffect extends Effect<Location, ItemCollection> {
    private static final BlockData AIR = Material.AIR.createBlockData();

    public BreakEffect(Map<Object, Object> configuration) {
        super(configuration);
    }

    @Override
    public ItemCollection run(Minion minion, Location argument) {
        Block block = argument.getBlock();
        Collection<ItemStack> drops = block.getDrops();
        World world = argument.getWorld();

        if (world != null) {
            world.setBlockData(argument, AIR);
        }

        // TODO: Block integration get block, event call
        return new ItemCollection(drops);
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
