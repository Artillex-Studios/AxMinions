package com.artillexstudios.axminions.api.minions

import org.bukkit.block.BlockFace

enum class Direction(val yaw: Float, val facing: BlockFace) {
    NORTH(180f, BlockFace.NORTH),
    WEST(90f, BlockFace.WEST),
    SOUTH(0f, BlockFace.SOUTH),
    EAST(-90f, BlockFace.EAST);
}