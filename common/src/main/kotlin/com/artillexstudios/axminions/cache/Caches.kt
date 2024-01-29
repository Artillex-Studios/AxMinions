package com.artillexstudios.axminions.cache

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.World

object Caches {
    private val map = ConcurrentHashMap<UUID, ChunkCache>()

    fun add(cache: ChunkCache) {
        map[cache.world.uid] = cache
    }

    fun remove(world: World) {
        map.remove(world.uid)
    }

    fun get(world: World): ChunkCache? {
        return map[world.uid]
    }
}