package com.artillexstudios.axminions.integrations.implementation.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Function;

public final class DefaultBlockIntegrable implements BlockIntegrable<Material> {

    @Override
    public Collection<ItemStack> lootAndBreak(Block block, ItemStack itemStack, Function<Material, Material> typeTransformer) {
        Material type = block.getType();
        Collection<ItemStack> drops = block.getDrops(itemStack);

        block.setType(typeTransformer.apply(type), true);
        return drops;
    }
}
