package com.artillexstudios.axminions.integrations.implementation.storage;

import com.artillexstudios.axminions.integrations.Integration;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public final class StorageIntegration extends Integration<StorageIntegrable> {
    private final Object2ObjectLinkedOpenHashMap<Location, ObjectArrayList<ItemStack>> items = new Object2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectLinkedOpenHashMap<Location, ObjectArrayList<ItemStack>> drops = new Object2ObjectLinkedOpenHashMap<>();

    @Override
    public void reload0() {
        this.register(new DefaultStorageIntegrable());
    }

    public boolean isFull(Location location) {
        for (StorageIntegrable integration : this.integrations()) {
            if (integration.isFull(location)) {
                return true;
            }
        }

        return false;
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

    public void pushDrop(Location location, ItemStack... itemStacks) {
        ObjectArrayList<ItemStack> items = this.drops.computeIfAbsent(location, k -> new ObjectArrayList<>());

        for (ItemStack itemStack : itemStacks) {
            for (ItemStack item : items) {
                if (item.isSimilar(itemStack)) {
                    itemStack.setAmount(itemStack.getAmount() + item.getAmount());
                }
            }
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

    public void flushDrops(Location location) {
        ObjectArrayList<ItemStack> items = this.drops.remove(location);
        if (items == null) {
            return;
        }

        World world = location.getWorld();
        if (world == null) {
            return;
        }

        for (ItemStack item : items) {
            world.dropItem(location, item);
        }
    }
}
