package com.artillexstudios.axminions.api.warnings

import com.artillexstudios.axapi.hologram.HologramFactory
import com.artillexstudios.axminions.api.minions.Minion
import net.kyori.adventure.text.Component

abstract class Warning(private val name: String) {

    fun getName(): String {
        return this.name
    }

    abstract fun getContent(): Component

    fun display(minion: Minion) {
        if (minion.getWarning() == null) {
            val hologram = HologramFactory.get()
                .spawnHologram(minion.getLocation().add(0.0, 0.15, 0.0), minion.getLocation().toString())
            hologram.addLine(getContent())
            minion.setWarning(this)
        }
    }
}