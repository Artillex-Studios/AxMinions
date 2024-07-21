package com.artillexstudios.axminions.integrations.implementation.storage;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

public final class DefaultStorageIntegrable implements StorageIntegrable {

    @Override
    public boolean flush(Location location, ObjectArrayList<ItemStack> items) {
        BlockState state = location.getBlock().getState();
        if (state instanceof Container container) {
            container.getInventory().addItem(items.elements());
            return true;
        }

        return false;
    }
}
