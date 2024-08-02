package com.artillexstudios.axminions.integrations.implementation.block;

import com.artillexstudios.axminions.utils.LogUtils;
import me.kryniowesegryderiusz.kgenerators.Main;
import me.kryniowesegryderiusz.kgenerators.generators.locations.objects.GeneratorLocation;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public final class KGeneratorsIntegrable implements BlockIntegrable {

    @Override
    public Collection<ItemStack> lootAndBreak(Location location, ItemStack itemStack) {
        if (!Main.getPlacedGenerators().isChunkFullyLoaded(location)) {
            LogUtils.debug("Chunk is not fully loaded");
            return null;
        }

        GeneratorLocation generatorLocation = Main.getPlacedGenerators().getLoaded(location);
        if (generatorLocation != null) {
            if (!generatorLocation.isBlockPossibleToMine(location)) {
                return null;
            }

            generatorLocation.scheduleGeneratorRegeneration();
            return List.of(generatorLocation.getGenerator().drawGeneratedObject().getCustomDrops().getItem());
        }

        return List.of();
    }
}
