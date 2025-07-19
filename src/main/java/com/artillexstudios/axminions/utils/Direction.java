package com.artillexstudios.axminions.utils;

import org.bukkit.block.BlockFace;

public enum Direction {
    NORTH(180f, BlockFace.NORTH),
    WEST(90f, BlockFace.WEST),
    SOUTH(0f, BlockFace.SOUTH),
    EAST(-90f, BlockFace.EAST);

    public static final Direction[] entries = Direction.values();
    private final float yaw;
    private final BlockFace face;

    Direction(float yaw, BlockFace face) {
        this.yaw = yaw;
        this.face = face;
    }

    public float yaw() {
        return this.yaw;
    }

    public BlockFace blockFace() {
        return this.face;
    }
}
