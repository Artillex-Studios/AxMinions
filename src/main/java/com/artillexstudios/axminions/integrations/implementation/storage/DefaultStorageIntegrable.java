package com.artillexstudios.axminions.integrations.implementation.storage;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public final class DefaultStorageIntegrable implements StorageIntegrable {

    @Override
    public boolean isFull(Location location) {
        BlockState state = location.getBlock().getState();
        if (state instanceof Container container) {
            return container.getInventory().firstEmpty() == -1;
        }

        return true;
    }

    @Override
    public boolean flush(Location location, ObjectArrayList<ItemStack> items) {
        BlockState state = location.getBlock().getState();
        if (state instanceof Container container) {
            HashMap<Integer, ItemStack> remaining = container.getInventory().addItem(items.elements());
            remaining.forEach((slot, item) -> {
                World world = location.getWorld();
                if (world == null) {
                    return;
                }

                world.dropItem(location, item);
            });
            return true;
        }

        return false;
    }
}
