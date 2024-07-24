package com.artillexstudios.axminions.integrations.implementation.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public final class DefaultBlockIntegrable implements BlockIntegrable {

    @Override
    public Collection<ItemStack> lootAndBreak(Location location, ItemStack itemStack) {
        Block block = location.getBlock();
        Collection<ItemStack> drops = block.getDrops(itemStack);

        block.setType(Material.AIR, true);
        return drops;
    }
}
