package com.artillexstudios.axminions.list

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * Teleports players next to a minion. A minion is only an armor stand, so the player can't be put at its location.
 */
internal object MinionTeleport {
    private const val EYE_HEIGHT = 1.62

    // Blocks that hurt or trap whoever stands in or on them, the liquids are checked separately
    private val harmful = setOf(
        Material.FIRE,
        Material.SOUL_FIRE,
        Material.CAMPFIRE,
        Material.SOUL_CAMPFIRE,
        Material.MAGMA_BLOCK,
        Material.CACTUS,
        Material.SWEET_BERRY_BUSH,
        Material.WITHER_ROSE,
        Material.POWDER_SNOW,
        Material.COBWEB,
        Material.NETHER_PORTAL,
        Material.END_PORTAL,
        Material.END_GATEWAY
    )

    // Every spot around a minion, the closest ones first. The spot of the minion itself is the last resort.
    private val offsets = buildList<Triple<Int, Int, Int>> {
        for (dx in -2..2) {
            for (dy in -1..1) {
                for (dz in -2..2) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        add(Triple(dx, dy, dz))
                    }
                }
            }
        }

        sortBy { (x, y, z) -> x * x + y * y + z * z }
        add(Triple(0, 0, 0))
    }

    // Folia doesn't allow Entity#teleport, but the Spigot API we compile against doesn't have this method.
    private val teleportAsync: Method? = try {
        Entity::class.java.getMethod("teleportAsync", Location::class.java)
    } catch (_: NoSuchMethodException) {
        null
    }

    /**
     * Finds somewhere to stand next to the minion, facing it, or null if there is no safe spot.
     * Has to be called on the thread that owns the location of the minion.
     */
    fun findSpot(minion: Location): Location? {
        val world = minion.world ?: return null
        val lookAt = minion.toVector().add(Vector(0.0, 0.5, 0.0))

        for ((dx, dy, dz) in offsets) {
            val x = minion.blockX + dx
            val y = minion.blockY + dy
            val z = minion.blockZ + dz
            if (!isSafe(world, x, y, z)) continue

            val spot = Location(world, x + 0.5, y.toDouble(), z + 0.5)
            if (!world.worldBorder.isInside(spot)) continue

            spot.setDirection(lookAt.clone().subtract(spot.toVector().add(Vector(0.0, EYE_HEIGHT, 0.0))))
            return spot
        }

        return null
    }

    /**
     * Teleports the player and calls [onSuccess] if that worked, which can be on any thread.
     */
    fun teleport(player: Player, location: Location, onSuccess: () -> Unit) {
        val method = teleportAsync
        if (method == null) {
            if (player.teleport(location)) {
                onSuccess()
            }
            return
        }

        @Suppress("UNCHECKED_CAST")
        (method.invoke(player, location) as CompletableFuture<Boolean>).thenAccept {
            if (it) {
                onSuccess()
            }
        }
    }

    private fun isSafe(world: World, x: Int, y: Int, z: Int): Boolean {
        if (y - 1 < world.minHeight || y + 1 >= world.maxHeight) return false

        return isFloor(world.getBlockAt(x, y - 1, z)) && isFree(world.getBlockAt(x, y, z)) && isFree(world.getBlockAt(x, y + 1, z))
    }

    private fun isFloor(block: Block): Boolean {
        if (block.type in harmful) return false

        // Not liquids (those have no collision), and nothing taller than a block, like fences
        val boxes = block.collisionShape.boundingBoxes
        return boxes.isNotEmpty() && boxes.maxOf { it.maxY } <= 1.0
    }

    private fun isFree(block: Block): Boolean {
        return block.isPassable && !block.isLiquid && block.type !in harmful
    }
}
