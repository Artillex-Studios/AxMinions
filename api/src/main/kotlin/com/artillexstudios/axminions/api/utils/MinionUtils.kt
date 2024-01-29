package com.artillexstudios.axminions.api.utils

import java.util.LinkedList
import java.util.Queue
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

object MinionUtils {
    private val FACES =
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

    @JvmStatic
    fun isStoneGenerator(location: Location): Boolean {
        var lava = false
        var water = false

        val locBlock = location.block

        FACES.fastFor {
            val relative = locBlock.getRelative(it)
            val type = relative.type
            if (!lava) {
                lava = type == Material.LAVA
            }

            if (!water) {
                water = type == Material.WATER
            }

            if (water && lava) {
                return true
            }
        }


        return false
    }

    @JvmStatic
    fun getTree(startBlock: Block): Set<Block> {
        val queue: Queue<Block> = LinkedList()
        val visited = mutableSetOf<Block>()
        val tree = mutableSetOf<Block>()

        queue.add(startBlock)

        while (queue.isNotEmpty()) {
            val block = queue.poll()

            val type = block.type.toString()
            if (type.endsWith("_WOOD") || type.endsWith("_LOG")) {
                tree.add(block)

                FACES.fastFor {
                    val relative = block.getRelative(it)
                    if (!visited.contains(relative)) {
                        queue.add(relative)
                        visited.add(relative)
                    }
                }
            }
        }

        return tree
    }
}