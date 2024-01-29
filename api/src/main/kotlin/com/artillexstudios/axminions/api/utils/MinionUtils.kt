package com.artillexstudios.axminions.api.utils

import java.util.LinkedList
import java.util.Queue
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

object MinionUtils {
    val FACES =
        arrayOf(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

    @JvmStatic
    fun getPlant(block: Block): ArrayList<Block> {
        val blocks = arrayListOf<Block>()
        var loc = block.location.clone()

        for (i in block.y downTo -65) {
            loc.add(0.0, -1.0, 0.0)
            val locBlock = loc.block
            if (!(locBlock.type == block.type && locBlock.getRelative(BlockFace.DOWN) == block)) break

            blocks.add(locBlock)
        }

        loc = block.location.clone()
        for (i in block.y..328) {
            loc.add(0.0, 1.0, 0.0)
            val locBlock = loc.block
            if (locBlock.type != block.type) break

            blocks.add(locBlock)
        }

        return blocks
    }

    fun Location.relative(face: BlockFace): Location {
        return this.clone().add(face.modX.toDouble(), face.modY.toDouble(), face.modZ.toDouble())
    }
}