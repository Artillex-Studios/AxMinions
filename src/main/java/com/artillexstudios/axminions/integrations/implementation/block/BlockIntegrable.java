package com.artillexstudios.axminions.integrations.implementation.block;

import com.artillexstudios.axminions.integrations.Integrable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Function;

public interface BlockIntegrable<T> extends Integrable {

    default Collection<ItemStack> lootAndBreak(Location location, ItemStack itemStack, Function<T, T> typeTransformer) {
        return this.lootAndBreak(location.getBlock(), itemStack, typeTransformer);
    }

    Collection<ItemStack> lootAndBreak(Block block, ItemStack itemStack, Function<T, T> typeTransformer);
}
