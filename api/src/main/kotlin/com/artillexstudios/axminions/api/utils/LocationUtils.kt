package com.artillexstudios.axminions.api.utils

import kotlin.math.roundToInt
import org.bukkit.Location
import org.bukkit.block.BlockFace

object LocationUtils {

    @JvmStatic
    fun getAllBlocksInRadius(location: Location, radius: Double, filterEmpty: Boolean): ArrayList<Location> {
        // Approximate the volume of the sphere
        val blocks = ArrayList<Location>((2 * radius * radius * radius).toInt())

        val blockX = location.blockX
        val blockY = location.blockY
        val blockZ = location.blockZ

        val rangeX = (blockX - radius).rangeTo((blockX + radius)).step(1.0)
        val rangeY = (blockY - radius).rangeTo((blockY + radius)).step(1.0)
        val rangeZ = (blockZ - radius).rangeTo((blockZ + radius)).step(1.0)

        val radiusSquared = radius * radius
        val smallRadiusSquared = (radius - 1) * (radius - 1)

        for (x in rangeX) {
            for (y in rangeY) {
                for (z in rangeZ) {
                    val distance =
                        ((blockX - x) * (blockX - x) + ((blockZ - z) * (blockZ - z)) + ((blockY - y) * (blockY - y)))

                    if (distance < radiusSquared && !(filterEmpty && distance < smallRadiusSquared)) {
                        blocks.add(Location(location.world, x, y, z))
                    }
                }
            }
        }

        return blocks
    }

    fun getAllBlocksFacing(location: Location, radius: Double, face: BlockFace): ArrayList<Location> {
        val blocks = ArrayList<Location>(radius.toInt())

        val modX = face.modX
        val modZ = face.modZ

        for (i in 1..radius.roundToInt()) {
            blocks.add(location.clone().add(i * modX.toDouble(), 0.0, i * modZ.toDouble()))
        }

        return blocks
    }
}