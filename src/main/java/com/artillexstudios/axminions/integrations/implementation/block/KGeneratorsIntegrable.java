package com.artillexstudios.axminions.integrations.implementation.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class KGeneratorsIntegrable implements BlockIntegrable<Material> {

    @Override
    public Collection<ItemStack> lootAndBreak(Block block, ItemStack itemStack, Function<Material, Material> typeTransformer) {
        //        if (!Main.getPlacedGenerators().isChunkFullyLoaded(location)) {
//            LogUtils.warn("Chunk is not fully loaded");
//            return null;
//        }
//
//        GeneratorLocation generatorLocation = Main.getPlacedGenerators().getLoaded(location);
//        if (generatorLocation != null) {
//            if (!generatorLocation.isBlockPossibleToMine(location)) {
//                return null;
//            }
//
//            generatorLocation.scheduleGeneratorRegeneration();
//            return List.of(generatorLocation.getGenerator().drawGeneratedObject().getCustomDrops().getItem());
//        }

        return List.of();
    }
}
