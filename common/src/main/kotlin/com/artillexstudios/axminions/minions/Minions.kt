package com.artillexstudios.axminions.minions

import com.artillexstudios.axminions.api.minions.Minion
import java.util.Collections
import java.util.concurrent.ConcurrentLinkedQueue

object Minions {
    private val entities = ConcurrentLinkedQueue<Minion>()

    fun load(minion: Minion) {
        entities.add(minion)
    }

    fun remove(minion: Minion) {
        entities.remove(minion)
    }

    fun getMinions(): List<Minion> {
        return Collections.unmodifiableList(entities.toList())
    }
}