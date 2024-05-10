package com.artillexstudios.axminions.api.warnings.impl

import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.warnings.Warning

class WarningNoWaterNearby : Warning("no_water_nearby") {

    override fun getContent(): String {
        return Messages.NO_WATER_NEARBY_WARNING()
    }
}