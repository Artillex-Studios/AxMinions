package com.artillexstudios.axminions.api.warnings

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.warnings.impl.WarningContainerFull
import com.artillexstudios.axminions.api.warnings.impl.WarningNoCharge
import com.artillexstudios.axminions.api.warnings.impl.WarningNoContainer
import com.artillexstudios.axminions.api.warnings.impl.WarningNoTool
import com.artillexstudios.axminions.api.warnings.impl.WarningNoWaterNearby

object Warnings {
    private val WARNINGS = hashMapOf<String, Warning>()

    @JvmField
    val CONTAINER_FULL = register(WarningContainerFull())

    @JvmField
    val NO_CONTAINER = register(WarningNoContainer())

    @JvmField
    val NO_TOOL = register(WarningNoTool())

    @JvmField
    val NO_WATER_NEARBY = register(WarningNoWaterNearby())

    @JvmField
    val NO_CHARGE = register(WarningNoCharge())

    @JvmStatic
    fun register(warning: Warning): Warning {
        WARNINGS[warning.getName()] = warning

        return warning
    }

    @JvmStatic
    fun remove(minion: Minion, warning: Warning) {
        if (minion.getWarning() == warning) {
            minion.getWarningHologram()?.remove()
            minion.setWarningHologram(null)
            minion.setWarning(null)
        }
    }
}