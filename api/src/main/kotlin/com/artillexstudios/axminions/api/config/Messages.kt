package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings
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
        fun STATUS_WORKING() = AxMinionsAPI.INSTANCE.getMessages().get("status.working", "<#33FF33>● Working")
        @JvmStatic
        fun STATUS_STOPPED() = AxMinionsAPI.INSTANCE.getMessages().get("status.stopped", "<#FF3333>● Stopped")
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
        fun LINK_CHEST_PROTECTED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.chest-protected")
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
        @JvmStatic
        fun MINION_TYPE_DISABLED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("minion-type-disabled")
        @JvmStatic
        fun SHOP_BOUGHT() = AxMinionsAPI.INSTANCE.getMessages().get<String>("shop.bought")
        @JvmStatic
        fun SHOP_NOT_ENOUGH_MONEY() = AxMinionsAPI.INSTANCE.getMessages().get<String>("shop.not-enough-money")
        @JvmStatic
        fun SHOP_CURRENCY_UNAVAILABLE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("shop.currency-unavailable")
        @JvmStatic
        fun LIST_NO_MINIONS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.no-minions")
        @JvmStatic
        fun LIST_NO_MINIONS_OTHER() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.no-minions-other")
        @JvmStatic
        fun LIST_TELEPORT_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.success")
        @JvmStatic
        fun LIST_TELEPORT_SUCCESS_PAID() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.success-paid")
        @JvmStatic
        fun LIST_TELEPORT_NOT_ENOUGH_MONEY() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.not-enough-money")
        @JvmStatic
        fun LIST_TELEPORT_CURRENCY_UNAVAILABLE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.currency-unavailable")
        @JvmStatic
        fun LIST_TELEPORT_NO_SAFE_LOCATION() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.no-safe-location")
        @JvmStatic
        fun LIST_TELEPORT_WORLD_UNAVAILABLE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.world-unavailable")
        @JvmStatic
        fun LIST_TELEPORT_MINION_GONE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("list.teleport.minion-gone")
        @JvmStatic
        fun USERS_PROMPT() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.prompt")
        @JvmStatic
        fun USERS_CANCELLED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.cancelled")
        @JvmStatic
        fun USERS_ADDED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.added")
        @JvmStatic
        fun USERS_REMOVED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.removed")
        @JvmStatic
        fun USERS_ALREADY_TRUSTED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.already-trusted")
        @JvmStatic
        fun USERS_SELF() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.self")
        @JvmStatic
        fun USERS_NOT_FOUND() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.not-found")
        @JvmStatic
        fun USERS_OWNER_ONLY() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.owner-only")
        @JvmStatic
        fun USERS_NO_ACCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("users.no-access")
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
