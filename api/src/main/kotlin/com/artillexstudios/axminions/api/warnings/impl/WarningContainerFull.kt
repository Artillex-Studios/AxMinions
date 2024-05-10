package com.artillexstudios.axminions.api.warnings.impl

import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.warnings.Warning

class WarningContainerFull : Warning("container_full") {

    override fun getContent(): String {
        return Messages.CONTAINER_FULL_WARNING()
    }
}