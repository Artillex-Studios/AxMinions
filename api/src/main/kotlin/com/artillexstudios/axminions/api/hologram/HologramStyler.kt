package com.artillexstudios.axminions.api.hologram

import com.artillexstudios.axapi.hologram.page.HologramPage
import com.artillexstudios.axapi.hologram.page.TextDisplayHologramPage
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.packetentity.PacketEntity
import com.artillexstudios.axapi.packetentity.meta.entity.DisplayMeta
import com.artillexstudios.axapi.packetentity.meta.entity.TextDisplayMeta
import com.artillexstudios.axapi.utils.Vector3f
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

object HologramStyler {

    private val textDisplayField = runCatching {
        TextDisplayHologramPage::class.java.getDeclaredField("textDisplay").apply { isAccessible = true }
    }.getOrNull()

    private var warnedOnce = false

    private fun getPacketEntity(page: HologramPage<*, *>): PacketEntity? {
        val field = textDisplayField
        if (field == null || page !is TextDisplayHologramPage) {
            warnReflectionFailure("textDisplay field or page type unavailable (page=${page::class.java.name})")
            return null
        }

        val entity = runCatching { field.get(page) as? PacketEntity }.getOrElse {
            warnReflectionFailure("exception reading field: $it")
            null
        }
        if (entity == null) {
            warnReflectionFailure("field value was null or not a PacketEntity")
        }
        return entity
    }

    fun applyViewDistance(page: HologramPage<*, *>, section: Section) {
        val distance = (section.getDouble("visibility-distance", 10.0) ?: 10.0).toInt()
        val entity = getPacketEntity(page) ?: return
        entity.viewDistance(distance)
    }
    
    fun showTo(page: HologramPage<*, *>, player: Player) {
        getPacketEntity(page)?.addPairing(player)
    }

    fun hideTo(page: HologramPage<*, *>, player: Player) {
        getPacketEntity(page)?.removePairing(player)
    }

    private fun warnReflectionFailure(reason: String) {
        if (warnedOnce) return
        warnedOnce = true
        Bukkit.getLogger().warning(
            "[AxMinions] Could not apply hologram visibility-distance via reflection: $reason. " +
                "The 'visibility-distance' config option will have no effect until this is fixed (likely an axapi version mismatch)."
        )
    }

    fun apply(meta: TextDisplayMeta, section: Section) {
        when (val background = section.getString("background", "transparent")) {
            "default" -> meta.defaultBackground(true)
            "transparent" -> {
                meta.defaultBackground(false)
                meta.backgroundColor(0)
            }
            else -> {
                meta.defaultBackground(false)
                meta.backgroundColor(parseColor(background))
            }
        }

        meta.shadow(section.getBoolean("shadow", true) ?: true)
        meta.seeThrough(section.getBoolean("see-through", false) ?: false)

        val scale = (section.getDouble("scale", 1.0) ?: 1.0).toFloat()
        meta.scale(Vector3f(scale, scale, scale))

        meta.billboardConstrain(parseBillboard(section.getString("billboard-type", "CENTER")))
        meta.alignment(parseAlignment(section.getString("alignment", "CENTER")))
        meta.viewRange((section.getDouble("visibility-distance", 10.0) ?: 10.0).toFloat())
    }

    fun offset(base: Location, section: Section): Location {
        return base.clone().add(
            section.getDouble("offset.x", 0.0) ?: 0.0,
            section.getDouble("offset.y", 0.0) ?: 0.0,
            section.getDouble("offset.z", 0.0) ?: 0.0
        )
    }

    private fun parseColor(value: String): Int {
        return runCatching { java.lang.Long.parseLong(value.removePrefix("#"), 16).toInt() }.getOrDefault(0)
    }

    private fun parseBillboard(value: String): DisplayMeta.BillboardConstrain {
        return runCatching { DisplayMeta.BillboardConstrain.valueOf(value.uppercase()) }.getOrDefault(DisplayMeta.BillboardConstrain.CENTER)
    }

    private fun parseAlignment(value: String): TextDisplayMeta.Alignment {
        return runCatching { TextDisplayMeta.Alignment.valueOf(value.uppercase()) }.getOrDefault(TextDisplayMeta.Alignment.CENTER)
    }
}
