package com.artillexstudios.axminions.integrations.implementation.block;

import com.artillexstudios.axminions.integrations.Integrable;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface BlockIntegrable extends Integrable {

    Collection<ItemStack> lootAndBreak(Location location, ItemStack itemStack);
}
