package com.artillexstudios.axminions.api.warnings.impl

import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.warnings.Warning

class WarningNoContainer : Warning("no_container") {

    override fun getContent(): String {
        return Messages.NO_CONTAINER_WARNING()
    }
}