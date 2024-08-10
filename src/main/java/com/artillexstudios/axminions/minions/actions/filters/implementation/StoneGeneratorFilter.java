package com.artillexstudios.axminions.minions.actions.filters.implementation;

import com.artillexstudios.axminions.exception.TransformerNotPresentException;
import com.artillexstudios.axminions.minions.actions.filters.Filter;
import com.artillexstudios.axminions.minions.actions.filters.Transformer;
import com.artillexstudios.axminions.utils.LogUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;

import java.util.List;
import java.util.Map;

public final class StoneGeneratorFilter extends Filter<Block> {
    private static final BlockFace[] FACES = new BlockFace[]{BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final int FACE_COUNT = 6;

    public StoneGeneratorFilter(Map<Object, Object> configuration) {
        this.addTransformer(Location.class, new Transformer<Location, Block>() {
            @Override
            public Block transform(Object object) {
                return ((Location) object).getBlock();
            }

            @Override
            public Class<?> inputClass() {
                return Location.class;
            }

            @Override
            public Class<?> outputClass() {
                return Block.class;
            }
        });

        this.addTransformer(Block.class, new Transformer<Block, Block>() {
            @Override
            public Block transform(Object object) {
                return (Block) object;
            }

            @Override
            public Class<?> inputClass() {
                return Block.class;
            }

            @Override
            public Class<?> outputClass() {
                return Block.class;
            }
        });
    }

    @Override
    public boolean isAllowed(Object object) {
        try {
            Transformer<?, Block> transformer = transformer(object.getClass());
            Block transformed = transformer.transform(object);
            if (transformed.getType() == Material.AIR) {
                return false;
            }

            boolean hasLava = false;
            boolean hasWater = false;
            for (int i = 0; i < FACE_COUNT; i++) {
                BlockFace face = FACES[i];
                Block relative = transformed.getRelative(face);
                Material type = relative.getType();

                if (!hasLava && type == Material.LAVA) {
                    if (hasWater) {
                        return true;
                    }

                    hasLava = true;
                    continue;
                }

                if (!hasWater) {
                    if (type == Material.WATER) {
                        if (hasLava) {
                            return true;
                        }

                        hasWater = true;
                    } else if (relative.getBlockData() instanceof Waterlogged blockData && blockData.isWaterlogged()) {
                        if (hasLava) {
                            return true;
                        }

                        hasWater = true;
                    }
                }
            }

            return false;
        } catch (TransformerNotPresentException exception) {
            LogUtils.error("No transformer found for input class {}!");
            return false;
        }
    }

    @Override
    public List<Class<?>> inputClasses() {
        return List.of(Location.class, Block.class);
    }
}
