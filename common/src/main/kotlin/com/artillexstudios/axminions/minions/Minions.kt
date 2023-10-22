package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.utils.ChunkPos
import java.util.Collections
import org.bukkit.Chunk

object Minions {
    private val mutex = Object()
    private val minions = arrayListOf<ChunkPos>()

    fun addTicking(chunk: Chunk) {
        val chunkX = chunk.x
        val chunkZ = chunk.z

        run breaking@{
            synchronized(mutex) {
                minions.forEach {
                    if (it.x == chunkX && it.z == chunkZ) {
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

        synchronized(mutex) {
            minions.forEach {
                if (it.x == chunkX && it.z == chunkZ) {
                    return true
                }
            }
        }

        return false
    }

    fun removeTicking(chunk: Chunk) {
        val chunkX = chunk.x
        val chunkZ = chunk.z

        run breaking@{
            synchronized(mutex) {
                minions.forEach {
                    if (it.x == chunkX && it.z == chunkZ) {
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

        synchronized(mutex) {
            var pos: ChunkPos? = null
            run breaking@{

                minions.forEach {
                    if (it.x == chunkX && it.z == chunkZ) {
                        pos = it
                        return@breaking
                    }
                }
            }

            if (pos === null) {
                pos = ChunkPos(chunkX, chunkZ)
                minions.add(pos!!)
            }


            pos!!.addMinion(minion)
        }
    }

    fun remove(minion: Minion) {
        val chunkX = round(minion.getLocation().x) shr 4
        val chunkZ = round(minion.getLocation().z) shr 4

        synchronized(mutex) {
            val iterator = minions.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if (next.x == chunkX && next.z == chunkZ) {
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
        synchronized(mutex) {
            minions.forEach {
                list.addAll(it.minions)
            }

            return Collections.unmodifiableList(list)
        }
    }

    internal fun get(): ArrayList<ChunkPos> {
        synchronized(mutex) {
            return minions
        }
    }

    private infix fun round(double: Double): Int {
        return (double + 0.5).toInt()
    }
}