package com.artillexstudios.axminions.api.warnings.impl

import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.warnings.Warning

class WarningNoTool : Warning("no_tool") {

    override fun getContent(): String {
        return Messages.NO_TOOL_WARNING()
    }
}