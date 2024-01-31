package com.artillexstudios.axminions.api.minions.utils

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.World

data class ChunkPos(val world: World, val x: Int, val z: Int, @Volatile var ticking: Boolean) {
    val minions = arrayListOf<Minion>()
    val worldUUID = world.uid

    fun addMinion(minion: Minion) {
        minions.add(minion)
    }

    fun removeMinion(minion: Minion): Boolean {
        minions.remove(minion)

        return minions.isEmpty()
    }

    fun setTicking(ticking: Boolean) {
        this.ticking = ticking

        minions.forEach {
            it.setTicking(true)
        }
    }

    override fun toString(): String {
        return "ChunkPos{x=$x,z=$z,minions=$minions}"
    }
}