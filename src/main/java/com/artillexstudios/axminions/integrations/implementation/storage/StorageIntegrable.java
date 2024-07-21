package com.artillexstudios.axminions.integrations.implementation.storage;

import com.artillexstudios.axminions.integrations.Integrable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface StorageIntegrable extends Integrable {

    boolean flush(Location location, ObjectArrayList<ItemStack> items);
}
