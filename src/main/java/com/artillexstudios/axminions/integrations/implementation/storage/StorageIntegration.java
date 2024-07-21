package com.artillexstudios.axminions.integrations.implementation.storage;

import com.artillexstudios.axminions.integrations.Integration;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class StorageIntegration extends Integration<StorageIntegrable> {
    private final Object2ObjectLinkedOpenHashMap<Location, ObjectArrayList<ItemStack>> items = new Object2ObjectLinkedOpenHashMap<>();

    public StorageIntegration() {
        this.register(new DefaultStorageIntegrable());
    }

    public void push(Location location, ItemStack... itemStacks) {
        ObjectArrayList<ItemStack> items = this.items.get(location);
        if (items != null) {
            items.addElements(items.size() - 1, itemStacks);
        } else {
            items = new ObjectArrayList<>(itemStacks);
            this.items.put(location, items);
        }
    }

    public void flush(Location location) {
        ObjectArrayList<ItemStack> items = this.items.remove(location);
        if (items == null) {
            return;
        }

        for (StorageIntegrable integration : this.integrations()) {
            if (integration.flush(location, items)) {
                return;
            }
        }
    }
}
