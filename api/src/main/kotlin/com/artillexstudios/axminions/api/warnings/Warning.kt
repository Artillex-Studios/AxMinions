package com.artillexstudios.axminions.api.warnings

import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axapi.hologram.HologramTypes
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axapi.packetentity.meta.entity.DisplayMeta
import com.artillexstudios.axapi.packetentity.meta.entity.TextDisplayMeta
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.minions.Minion

abstract class Warning(private val name: String) {

    fun getName(): String {
        return this.name
    }

    abstract fun getContent(): String

    fun display(minion: Minion) {
        if (!Config.DISPLAY_WARNINGS()) return

        if (minion.getWarning() == null) {
            val hologram = Hologram(minion.getLocation().clone().add(0.0, 1.35, 0.0))
            val page = hologram.createPage(HologramTypes.TEXT)
            page.setEntityMetaHandler { meta ->
                val textDisplayMeta = meta as TextDisplayMeta
                textDisplayMeta.seeThrough(true)
                textDisplayMeta.alignment(TextDisplayMeta.Alignment.CENTER)
                textDisplayMeta.billboardConstrain(DisplayMeta.BillboardConstrain.CENTER)
            }
            page.setContent(StringUtils.formatToString(this.getContent()))
            page.spawn()
            minion.setWarning(this)
            minion.setWarningHologram(hologram)
        }
    }
}