package com.artillexstudios.axminions.cache

import java.util.concurrent.ConcurrentHashMap

class ChunkLayer {
    private val map = ConcurrentHashMap<Int, Any>()

    fun set(x: Int, z: Int, to: Any): Any {
        val key = (x shl 16) or z

        map[key] = to
        return to
    }

    fun get(x: Int, z: Int): Any? {
        val key = (x shl 16) or z

        return map[key]
    }
}