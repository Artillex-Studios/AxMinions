package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.utils.ChunkPos
import com.artillexstudios.axminions.utils.fastFor
import java.util.Collections
import org.bukkit.Chunk

object Minions {
    private val mutex = Object()
    private val minions = hashMapOf<ChunkPos, ArrayList<Minion>>()
    private val mutableChunkPos = ChunkPos(0, 0)

    fun addTicking(chunk: Chunk) {
        synchronized(mutex) {
            mutableChunkPos.x = chunk.x
            mutableChunkPos.z = chunk.z

            minions[mutableChunkPos]?.fastFor {
                it.setTicking(true)
            } ?: return
        }
    }

    fun removeTicking(chunk: Chunk) {
        synchronized(mutex) {
            mutableChunkPos.x = chunk.x
            mutableChunkPos.z = chunk.z

            val minions = this.minions[mutableChunkPos] ?: return

            minions.fastFor {
                it.setTicking(false)
            }
        }
    }

    fun load(minion: Minion) {
        synchronized(mutex) {
            mutableChunkPos.x = round(minion.getLocation().x) shr 4
            mutableChunkPos.z = round(minion.getLocation().z) shr 4
            val pos = minions[mutableChunkPos] ?: arrayListOf()

            pos.add(minion)
            minions[mutableChunkPos] = pos
        }
    }

    fun remove(minion: Minion) {
        synchronized(mutex) {
            mutableChunkPos.x = minion.getLocation().blockX shr 4
            mutableChunkPos.z = minion.getLocation().blockZ shr 4
            val pos = minions[mutableChunkPos] ?: return

            pos.remove(minion)
            if (pos.isEmpty()) {
                minions.remove(mutableChunkPos)
            }
        }
    }

    fun getMinions(): List<Minion> {
        synchronized(mutex) {
            val list = mutableListOf<Minion>()
            minions.forEach { (_, value) ->
                list.addAll(value)
            }
            return Collections.unmodifiableList(list)
        }
    }

    internal fun get(): HashMap<ChunkPos, ArrayList<Minion>> {
        synchronized(mutex) {
            return minions
        }
    }

    private infix fun round(double: Double): Int {
        return (double + 0.5).toInt()
    }
}