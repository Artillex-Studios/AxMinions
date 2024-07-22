package com.artillexstudios.axminions.utils;

import org.bukkit.block.BlockFace;

public enum Direction {
    NORTH(180f, BlockFace.NORTH),
    WEST(90f, BlockFace.WEST),
    SOUTH(0f, BlockFace.SOUTH),
    EAST(-90f, BlockFace.EAST);

    private final float yaw;
    private final BlockFace face;

    Direction(float yaw, BlockFace face) {
        this.yaw = yaw;
        this.face = face;
    }
}
