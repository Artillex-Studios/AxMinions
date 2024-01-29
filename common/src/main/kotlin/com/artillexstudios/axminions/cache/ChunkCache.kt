package com.artillexstudios.axminions.cache

import com.artillexstudios.axminions.AxMinionsPlugin
import java.util.concurrent.ConcurrentHashMap
import me.kryniowesegryderiusz.kgenerators.Main
import org.bukkit.Location
import org.bukkit.World

class ChunkCache(val world: World) {
    private val map = ConcurrentHashMap<ChunkLoc, HashMap<Int, ChunkLayer>>()

    fun get(x: Int, y: Int, z: Int): Any {
        val pos = ChunkLoc(x shr 4, z shr 4)
        val layerMap = map[pos] ?: HashMap()
        val layer = layerMap[y] ?: ChunkLayer()
        layerMap[y] = layer

        val worldBlock: Any
        if (AxMinionsPlugin.integrations.kGeneratorsIntegration) {
            val loc = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            if (!Main.getPlacedGenerators().isChunkFullyLoaded(loc)) {
                worldBlock = world.getBlockAt(x, y, z).type
            } else {
                val gen = Main.getPlacedGenerators().getLoaded(loc) ?: world.getBlockAt(x, y, z).type
                worldBlock = gen
            }
        } else {
            worldBlock = world.getBlockAt(x, y, z).type
        }

        val block = layer.get(x, z) ?: layer.set(x, z, worldBlock)
        map[pos] = layerMap

        return block
    }

    fun invalidate(x: Int, z: Int) {
        map.remove(ChunkLoc(x, z))
    }

    fun update(x: Int, y: Int, z: Int) {
        get(x, y, z)
    }
}