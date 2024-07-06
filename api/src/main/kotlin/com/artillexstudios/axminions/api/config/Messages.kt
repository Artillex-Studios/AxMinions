package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.RotationType
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Direction
import java.io.File
import java.io.InputStream
import java.util.Locale

class Messages(file: File, stream: InputStream) {
    companion object {
        @JvmStatic
        fun PREFIX() = AxMinionsAPI.INSTANCE.getMessages().get<String>("prefix")
        @JvmStatic
        fun NO_CONTAINER_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-container")
        @JvmStatic
        fun NO_TOOL_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-tool")
        @JvmStatic
        fun NO_WATER_NEARBY_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-water-nearby")
        @JvmStatic
        fun CONTAINER_FULL_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.container-full")
        @JvmStatic
        fun TIME_DAY() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.day")
        @JvmStatic
        fun TIME_HOUR() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.hour")
        @JvmStatic
        fun TIME_MINUTE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.minute")
        @JvmStatic
        fun TIME_SECOND() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.second")
        @JvmStatic
        fun NO_CHARGE_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-charge")
        @JvmStatic
        fun RELOAD_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("reload")
        @JvmStatic
        fun PLACE_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.success")
        @JvmStatic
        fun PLACE_LIMIT_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.limit-reached")
        @JvmStatic
        fun PLACE_MINION_AT_LOCATION() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.minion-at-location")
        @JvmStatic
        fun PLACE_MISSING_PERMISSION() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.missing-permission")
        @JvmStatic
        fun ISLAND_LIMIT_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.island-limit-reached")
        @JvmStatic
        fun STATISTICS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("statistics")
        @JvmStatic
        fun LEVEL_COLOR(level: Int = 1) = AxMinionsAPI.INSTANCE.getMessages().get("levels.$level", "<#33FF33>")
        @JvmStatic
        fun UPGRADES_MAX_LEVEL_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("upgrades.limit-reached")
        @JvmStatic
        fun ROTATION_NAME(direction: Direction) = AxMinionsAPI.INSTANCE.getMessages().get("directions.${direction.name.lowercase(
            Locale.ENGLISH)}", direction.name)
        @JvmStatic
        fun CHARGE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("charge.charge")
        @JvmStatic
        fun CHARGE_FAIL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("charge.not-enough-money")
        @JvmStatic
        fun CHARGE_NOT_ENOUGH_TIME_PASSED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("charge.not-enough-time-passed")
        @JvmStatic
        fun WRONG_TOOL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("tools.wrong-tool")
        @JvmStatic
        fun ERROR_INVENTORY_FULL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("errors.inventory-full")
        @JvmStatic
        fun LINK_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.success")
        @JvmStatic
        fun LINK_UNLINK() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.unlink")
        @JvmStatic
        fun LINK_FAIL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.fail")
        @JvmStatic
        fun LINK_START() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.start")
        @JvmStatic
        fun UPGRADE_FAIL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("upgrades.fail")
        @JvmStatic
        fun RESET() = AxMinionsAPI.INSTANCE.getMessages().get<String>("reset")
        @JvmStatic
        fun NOT_ON_ISLAND() = AxMinionsAPI.INSTANCE.getMessages().get<String>("not-on-island")
        @JvmStatic
        fun SLOT_GIVE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("slot-give")
        @JvmStatic
        fun SLOT_RECEIVE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("slot-receive")
        @JvmStatic
        fun LOCATION_FORMAT() = AxMinionsAPI.INSTANCE.getMessages().get<String>("location-format")
    }

    private val config = Config(
        file,
        stream,
        GeneralSettings.builder().setUseDefaults(false).build(),
        LoaderSettings.builder().setAutoUpdate(true).build(),
        DumperSettings.DEFAULT,
        UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
    )

    fun <T> get(route: String?): T {
        return this.config.get(route)
    }

    fun <T> get(route: String?, default: T): T {
        return this.config.get(route, default)
    }

    fun getFormatted(route: String?): String {
        return StringUtils.formatToString(this.config.get(route))
    }

    fun reload() {
        config.reload()
    }
}