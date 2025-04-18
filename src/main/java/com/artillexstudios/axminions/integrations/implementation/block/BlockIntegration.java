package com.artillexstudios.axminions.integrations.implementation.block;

import com.artillexstudios.axminions.integrations.Integration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class BlockIntegration extends Integration<BlockIntegrable> {

    @Override
    public void reload0() {
        if (Bukkit.getPluginManager().getPlugin("KGenerators") != null) {
            this.register(new KGeneratorsIntegrable());
        }

        this.register(new DefaultBlockIntegrable());
    }

    public <T> Collection<ItemStack> lootAndBreak(Block block, ItemStack itemStack, Function<T, T> typeTransformer) {
        for (BlockIntegrable<Object> integration : this.integrations()) {
            Collection<ItemStack> items = integration.lootAndBreak(block, itemStack, (Function<Object, Object>) typeTransformer);
            if (items == null) {
                return null;
            }

            if (items.isEmpty()) {
                continue;
            }

            return items;
        }

        return List.of();
    }

    public <T> Collection<ItemStack> lootAndBreak(Location location, ItemStack itemStack, Function<T, T> typeTransformer) {
        for (BlockIntegrable<Object> integration : this.integrations()) {
            Collection<ItemStack> items = integration.lootAndBreak(location, itemStack, (Function<Object, Object>) typeTransformer);
            if (items == null) {
                return null;
            }

            if (items.isEmpty()) {
                continue;
            }

            return items;
        }

        return List.of();
    }
}
