package com.artillexstudios.axminions.api.minions.utils

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.fastFor
import org.bukkit.World

data class ChunkPos(val world: World, var x: Int, var z: Int) {
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
        minions.fastFor {
            it.setTicking(ticking)
        }
    }

    override fun toString(): String {
        return "ChunkPos{x=$x,z=$z,minions=$minions}"
    }
}