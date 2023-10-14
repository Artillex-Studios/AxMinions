package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.utils.ChunkPos
import com.artillexstudios.axminions.api.utils.fastFor
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Chunk

object Minions {
    private val minions = ConcurrentHashMap<ChunkPos, ArrayList<Minion>>()

    fun addTicking(chunk: Chunk) {
        val pos = ChunkPos(chunk.x, chunk.z)

        minions[pos]?.fastFor {
            it.setTicking(true)
        } ?: return
        println("LOADING CHUNK! X: ${chunk.x} Z: ${chunk.z}")
    }

    fun isTicking(chunk: Chunk): Boolean {
        val pos = ChunkPos(chunk.x, chunk.z)

        return minions.contains(pos)
    }

    fun removeTicking(chunk: Chunk) {
        val pos = ChunkPos(chunk.x, chunk.z)

        val minions = this.minions[pos] ?: return

        minions.fastFor {
            it.setTicking(false)
        }
    }

    fun load(minion: Minion) {
        println("LOADING MINION!!")
        val chunkPos = ChunkPos(minion.getLocation().chunk.x, minion.getLocation().chunk.z)
        val pos = minions[chunkPos] ?: arrayListOf()

        pos.add(minion)
        minions[chunkPos] = pos
    }

    fun remove(minion: Minion) {
        val chunkPos = ChunkPos(minion.getLocation().chunk.x, minion.getLocation().chunk.z)

        val pos = minions[chunkPos] ?: return

        pos.remove(minion)
        if (pos.isEmpty()) {
            minions.remove(chunkPos)
        }
    }

    fun getMinions(): List<Minion> {
        val list = mutableListOf<Minion>()
        minions.forEach { (_, value) ->
            list.addAll(value)
        }
        return Collections.unmodifiableList(list)

    }

    internal fun get(): ConcurrentHashMap<ChunkPos, ArrayList<Minion>> {
        return minions
    }

    private infix fun round(double: Double): Int {
        return (double + 0.5).toInt()
    }
}