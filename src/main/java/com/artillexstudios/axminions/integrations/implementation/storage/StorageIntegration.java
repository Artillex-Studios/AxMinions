package com.artillexstudios.axminions.integrations.implementation.storage;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axminions.config.Config;
import com.artillexstudios.axminions.integrations.Integration;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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

    public void push(Location location, List<ItemStack> itemStacks) {
        ObjectArrayList<ItemStack> items = this.items.get(location);
        if (items != null) {
            items.addAll(itemStacks);
        } else {
            items = new ObjectArrayList<>(itemStacks);
            this.items.put(location, items);
        }
    }

    public void pushDrop(Location location, List<ItemStack> itemStacks) {
        ObjectArrayList<ItemStack> items = this.drops.computeIfAbsent(location, k -> new ObjectArrayList<>());
        if (Config.debug) {
            LogUtils.debug("Pushing drop, drops: {}, location: {}", this.drops, location);
        }

        for (ItemStack itemStack : itemStacks) {
            boolean contains = false;
            for (ItemStack item : items) {
                if (item.isSimilar(itemStack)) {
                    contains = true;
                    item.setAmount(itemStack.getAmount() + item.getAmount());
                    break;
                }
            }

            if (!contains) {
                items.add(itemStack);
            }
        }
    }

    public void flush(Location location) {
        if (Config.debug) {
            LogUtils.debug("Flush, items: {}, location: {}", this.items, location);
        }
        ObjectArrayList<ItemStack> items = this.items.remove(location);
        if (items == null) {
            if (Config.debug) {
                LogUtils.debug("No items");
            }
            return;
        }

        for (StorageIntegrable integration : this.integrations()) {
            if (integration.flush(location, items)) {
                return;
            }
        }
    }

    public void flushDrops(Location location) {
        if (Config.debug) {
            LogUtils.debug("Flushdrops, items: {}, location: {}", this.drops, location);
        }
        ObjectArrayList<ItemStack> items = this.drops.remove(location);
        if (items == null) {
            if (Config.debug) {
                LogUtils.debug("No items");
            }
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
