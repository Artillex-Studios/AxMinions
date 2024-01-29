package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.ChunkPos
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.MinionUtils.relative
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.cache.Caches
import java.util.Collections
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block

object Minions {
    val lock = ReentrantReadWriteLock()
    val minions = arrayListOf<ChunkPos>()

    fun addTicking(chunk: Chunk) {
        val chunkX = chunk.x
        val chunkZ = chunk.z
        val world = chunk.world

        run breaking@{
            lock.read {
                minions.forEach {
                    if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                        it.setTicking(true)
                        return@breaking
                    }
                }
            }
        }
    }

    fun isTicking(chunk: Chunk): Boolean {
        val chunkX = chunk.x
        val chunkZ = chunk.z
        val world = chunk.world

        lock.read {
            minions.forEach {
                if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                    return true
                }
            }
        }

        return false
    }

    fun removeTicking(chunk: Chunk) {
        val chunkX = chunk.x
        val chunkZ = chunk.z
        val world = chunk.world

        run breaking@{
            lock.read {
                minions.forEach {
                    if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                        it.setTicking(false)
                        return@breaking
                    }
                }
            }
        }
    }

    fun load(minion: Minion) {
        val chunkX = round(minion.getLocation().x) shr 4
        val chunkZ = round(minion.getLocation().z) shr 4
        val world = minion.getLocation().world ?: return

        lock.write {
            var pos: ChunkPos? = null
            run breaking@{

                minions.forEach {
                    if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                        pos = it
                        return@breaking
                    }
                }
            }

            if (pos === null) {
                pos = ChunkPos(world, chunkX, chunkZ)
                minions.add(pos!!)
            }


            pos!!.addMinion(minion)
        }
    }

    fun remove(minion: Minion) {
        val chunkX = round(minion.getLocation().x) shr 4
        val chunkZ = round(minion.getLocation().z) shr 4
        val world = minion.getLocation().world ?: return

        lock.write {
            val iterator = minions.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (world.uid == next.worldUUID && next.x == chunkX && next.z == chunkZ) {
                    if (next.removeMinion(minion)) {
                        iterator.remove()
                    }
                    break
                }
            }
        }
    }

    fun getMinions(): List<Minion> {
        val list = mutableListOf<Minion>()
        lock.read {
            minions.forEach {
                list.addAll(it.minions)
            }

            return Collections.unmodifiableList(list)
        }
    }

    internal inline fun get(use: (ArrayList<ChunkPos>) -> Unit) {
        lock.read {
            use(minions)
        }
    }

    private infix fun round(double: Double): Int {
        return (double + 0.5).toInt()
    }

    @JvmStatic
    fun getTree(startBlock: Location): Set<Block> {
        val queue: Queue<Location> = LinkedList()
        val visited = mutableSetOf<Location>()
        val tree = mutableSetOf<Block>()

        val cache = Caches.get(startBlock.world!!) ?: return tree
        queue.add(startBlock)

        while (queue.isNotEmpty()) {
            val block = queue.poll()

            val type = cache.get(block.x.toInt(), block.y.toInt(), block.z.toInt()) as? Material ?: continue
            if (type.name.endsWith("_WOOD") || type.name.endsWith("_LOG")) {
                tree.add(block.block)

                MinionUtils.FACES.fastFor {
                    val relative = block.relative(it)
                    if (!visited.contains(relative)) {
                        queue.add(relative)
                        visited.add(relative)
                    }
                }
            }
        }

        return tree
    }

    @JvmStatic
    fun isStoneGenerator(location: Location): Boolean {
        var lava = false
        var water = false

        val cache = Caches.get(location.world!!) ?: return false

        MinionUtils.FACES.fastFor {
            val relative = location.relative(it)
            val type = cache.get(relative.x.toInt(), relative.y.toInt(), relative.z.toInt()) as? Material ?: return@fastFor

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
}