package com.artillexstudios.axminions.api.warnings

import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axapi.hologram.HologramType
import com.artillexstudios.axapi.hologram.HologramTypes
import com.artillexstudios.axapi.hologram.page.HologramPage
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axapi.packetentity.meta.entity.TextDisplayMeta
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.hologram.HologramStyler
import com.artillexstudios.axminions.api.minions.Minion

abstract class Warning(private val name: String) {

    fun getName(): String {
        return this.name
    }

    abstract fun getContent(): String

    fun display(minion: Minion) {
        if (!Config.DISPLAY_WARNINGS()) return

        if (minion.getWarning() == null) {
            val section = Config.HOLOGRAM_WARNINGS_SECTION()
            val hologram = Hologram(HologramStyler.offset(minion.getLocation(), section))
            val page = hologram.createPage(HologramTypes.TEXT)
            page.setEntityMetaHandler { meta ->
                HologramStyler.apply(meta as TextDisplayMeta, section)
            }
            HologramStyler.applyViewDistance(page, section)
            page.content = StringUtils.formatToString(this.getContent());
            page.spawn();
            minion.setWarning(this)
            minion.setWarningHologram(hologram)
        }
    }
}