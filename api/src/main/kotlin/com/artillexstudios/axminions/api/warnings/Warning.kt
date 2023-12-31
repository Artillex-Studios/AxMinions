package com.artillexstudios.axminions.api.warnings

import com.artillexstudios.axapi.hologram.HologramFactory
import com.artillexstudios.axminions.api.config.Config
import net.kyori.adventure.text.Component
import com.artillexstudios.axminions.api.minions.Minion

abstract class Warning(private val name: String) {

    fun getName(): String {
        return this.name
    }

    abstract fun getContent(): Component

    fun display(minion: Minion) {
        if (!Config.DISPLAY_WARNINGS()) return

        if (minion.getWarning() == null) {
            val hologram = HologramFactory.get()
                .spawnHologram(minion.getLocation().clone().add(0.0, 1.35, 0.0), minion.getLocation().toString())
            hologram.addLine(getContent())
            minion.setWarning(this)
            minion.setWarningHologram(hologram)
        }
    }
}