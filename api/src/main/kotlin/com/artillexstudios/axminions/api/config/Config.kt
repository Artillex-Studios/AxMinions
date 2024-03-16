package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axminions.api.AxMinionsAPI
import java.io.File
import java.io.InputStream
import java.lang.ClassCastException
import java.util.Locale

class Config(file: File, stream: InputStream) {
    companion object {
        private var debug: Boolean? = null
        @JvmStatic
        fun AUTO_SAVE_MINUTES() = AxMinionsAPI.INSTANCE.getConfig().get("autosave-minutes", 3L)
        @JvmStatic
        fun MAX_LINKING_DISTANCE() = AxMinionsAPI.INSTANCE.getConfig().get("max-linking-distance", 30)
        @JvmStatic
        fun DEFAULT_MINION_LIMIT() = AxMinionsAPI.INSTANCE.getConfig().get("default-minion-limit", 5)
        @JvmStatic
        fun ONLY_OWNER_BREAK() = AxMinionsAPI.INSTANCE.getConfig().get("only-owner-break", true)
        @JvmStatic
        fun ONLY_OWNER_GUI() = AxMinionsAPI.INSTANCE.getConfig().get("only-owner-gui", true)
        @JvmStatic
        fun DISPLAY_WARNINGS() = AxMinionsAPI.INSTANCE.getConfig().get("display-warnings", true)
        @JvmStatic
        fun CAN_BREAK_TOOLS() = AxMinionsAPI.INSTANCE.getConfig().get("can-break-tools", true)
        @JvmStatic
        fun USE_DURABILITY() = AxMinionsAPI.INSTANCE.getConfig().get("use-durability", true)
        @JvmStatic
        fun DATABASE_TYPE() = AxMinionsAPI.INSTANCE.getConfig().get("database.type", "H2")
        @JvmStatic
        fun STACKER_HOOK() = AxMinionsAPI.INSTANCE.getConfig().get("hooks.stacker", "none")
        @JvmStatic
        fun ECONOMY_HOOK() = AxMinionsAPI.INSTANCE.getConfig().get("hooks.economy", "Vault")
        @JvmStatic
        fun PRICES_HOOK() = AxMinionsAPI.INSTANCE.getConfig().get("hooks.prices", "ShopGUIPlus")
        @JvmStatic
        fun CHARGE_ENABLED() = AxMinionsAPI.INSTANCE.getConfig().get("charge.enabled", false)
        @JvmStatic
        fun CHARGE_AMOUNT() = AxMinionsAPI.INSTANCE.getConfig().get("charge.amount", 1800)
        @JvmStatic
        fun MAX_CHARGE() = AxMinionsAPI.INSTANCE.getConfig().get("charge.max-charge", 1440)
        @JvmStatic
        fun CHARGE_PRICE() = AxMinionsAPI.INSTANCE.getConfig().get("charge.price", 10000.0)
        @JvmStatic
        fun TIMER_FORMAT() = AxMinionsAPI.INSTANCE.getConfig().get("timer-format", 1)
        @JvmStatic
        fun GUI_SIZE() = AxMinionsAPI.INSTANCE.getConfig().get<Int>("gui.size")
        @JvmStatic
        fun PULL_FROM_CHEST() = AxMinionsAPI.INSTANCE.getConfig().get("pull-tools-from-chest", false)
        @JvmStatic
        fun UPGRADE_FAIL() = AxMinionsAPI.INSTANCE.getConfig().get("upgrade-fail", "chat").lowercase(Locale.ENGLISH)
        @JvmStatic
        fun PLACE_PERMISSION() = AxMinionsAPI.INSTANCE.getConfig().get("place-permissions", false)
        @JvmStatic
        fun WORK_WHEN_OWNER_OFFLINE() = AxMinionsAPI.INSTANCE.getConfig().get("work-when-owner-offline", true)
        @JvmStatic
        fun MAX_BREAKS_PER_TICK() = AxMinionsAPI.INSTANCE.getConfig().get("max-breaks-per-tick", 200)
        @JvmStatic
        fun UPGRADE_SOUND() = AxMinionsAPI.INSTANCE.getConfig().get("upgrade-sound", "ENTITY_PLAYER_LEVELUP")
        @JvmStatic
        fun DEBUG(): Boolean {
            if (debug === null) {
                debug = AxMinionsAPI.INSTANCE.getConfig().get("debug", false)
            }

            return debug ?: false
        }
    }

    private val config = Config(
        file,
        stream,
        GeneralSettings.builder().setUseDefaults(false).build(),
        LoaderSettings.builder().setAutoUpdate(true).build(),
        DumperSettings.DEFAULT,
        UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
    )

    fun <T> get(route: String?, default: T): T {
        try {
            return this.config.get(route, default)
        } catch (exception: ClassCastException) {
            val value = this.config.getString(route, default.toString())
            return when (default!!::class) {
                Int::class -> {
                    value.toInt() as T
                }
                Double::class -> {
                    value.toDouble() as T
                }
                else -> {
                    value.toBoolean() as T
                }
            }
        }
    }

    fun <T> get(route: String?): T {
        return this.config.get(route)
    }

    fun getConfig(): Config {
        return config
    }

    fun reload() {
        config.reload()
    }
}