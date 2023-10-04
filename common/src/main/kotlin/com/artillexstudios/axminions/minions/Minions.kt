package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import java.util.Collections

object Minions {
    private val entities = mutableListOf<Minion>()

    fun load(minion: Minion) {
        entities.add(minion)
    }

    fun remove(minion: Minion) {
        entities.remove(minion)
    }

    fun getMinions(): List<Minion> {
        return Collections.unmodifiableList(entities)
    }
}