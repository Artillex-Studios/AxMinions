package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axminions.api.AxMinionsAPI
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import java.io.File
import java.io.InputStream

class Config(file: File, stream: InputStream) {
    companion object {
        @JvmStatic
        fun AUTO_SAVE_MINUTES() = AxMinionsAPI.INSTANCE.getConfig().get("autosave-minutes", 3L)
        @JvmStatic
        fun MAX_LINKING_DISTANCE() = AxMinionsAPI.INSTANCE.getConfig().get("max-linking-distance", 30)
        @JvmStatic
        fun DEFAULT_MINION_LIMIT() = AxMinionsAPI.INSTANCE.getConfig().get("default-minion-limit", 5)
        @JvmStatic
        fun ALLOW_FLOATING_MINIONS() = AxMinionsAPI.INSTANCE.getConfig().get("allow-floating-minions", false)
        @JvmStatic
        fun ONLY_OWNER_BREAK() = AxMinionsAPI.INSTANCE.getConfig().get("only-owner-break", true)
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
        return this.config.get(route, default)
    }

    fun <T> get(route: String?): T {
        return this.config.get(route)
    }

    fun reload() {
        config.reload()
    }
}