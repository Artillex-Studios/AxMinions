package com.artillexstudios.axminions.api.warnings.impl

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.warnings.Warning
import net.kyori.adventure.text.Component

class WarningNoTool : Warning("no_tool") {

    override fun getContent(): Component {
        return StringUtils.format(Messages.NO_TOOL_WARNING)
    }
}