package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.utils.ChunkPos
import com.artillexstudios.axapi.scheduler.Scheduler
import java.util.Collections
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import org.bukkit.Bukkit
import org.bukkit.Chunk

object Minions {
    internal val lock = ReentrantReadWriteLock()
    internal val minions = arrayListOf<ChunkPos>()

    fun startTicking(chunk: Chunk) {
        val chunkX = chunk.x
        val chunkZ = chunk.z
        val world = chunk.world

        if (!Bukkit.isPrimaryThread()) { 
            Scheduler.get().run { a ->
                run breaking@{
                    minions.forEach {
                        if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                            it.setTicking(true)
                            return@breaking
                        }
                    }
                }
            }
        } else {
            run breaking@{
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

        minions.forEach {
            if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                return it.ticking
            }
        }
        
        return false
    }

    fun stopTicking(chunk: Chunk) {
        val chunkX = chunk.x
        val chunkZ = chunk.z
        val world = chunk.world

        if (!Bukkit.isPrimaryThread()) {
            Scheduler.get().run {  a ->
                run breaking@{
                    minions.forEach {
                        if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                            it.setTicking(false)
                            return@breaking
                        }
                    }
                }
            }
        } else {
            run breaking@{
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
        val chunkX = minion.getLocation().blockX shr 4
        val chunkZ = minion.getLocation().blockZ shr 4
        val world = minion.getLocation().world ?: return

        if (!Bukkit.isPrimaryThread()) {
            Scheduler.get().run { a ->
                var pos: ChunkPos? = null
                run breaking@{
                    minions.forEach {
                        if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                            pos = it
                            return@breaking
                        }
                    }
                }

                if (pos == null) {
                    pos = ChunkPos(world, chunkX, chunkZ, false)
                    minions.add(pos!!)
                }

                pos!!.addMinion(minion)
            }
        } else {
            var pos: ChunkPos? = null
            run breaking@{
                minions.forEach {
                    if (world.uid == it.worldUUID && it.x == chunkX && it.z == chunkZ) {
                        pos = it
                        return@breaking
                    }
                }
            }

            if (pos == null) {
                pos = ChunkPos(world, chunkX, chunkZ, false)
                minions.add(pos!!)
            }

           pos!!.addMinion(minion)
        }
    }

    fun remove(minion: Minion) {
        val chunkX = minion.getLocation().blockX shr 4
        val chunkZ = minion.getLocation().blockZ shr 4
        val world = minion.getLocation().world ?: return

        if (!Bukkit.isPrimaryThread()) {
            Scheduler.get().run { a ->
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
        } else {
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

    internal inline fun get(minions: (ArrayList<ChunkPos>) -> Unit) {
        lock.read {
            minions(this.minions)
        }
    }
}