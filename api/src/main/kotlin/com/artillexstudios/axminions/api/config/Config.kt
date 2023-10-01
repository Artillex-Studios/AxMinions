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
        @JvmField
        val AUTO_SAVE_MINUTES = AxMinionsAPI.INSTANCE.getConfig().get("autosave-minutes", 3L)
        @JvmField
        val MAX_LINKING_DISTANCE = AxMinionsAPI.INSTANCE.getConfig().get("max-linking-distance", 30)
        @JvmField
        val DEFAULT_MINION_LIMIT = AxMinionsAPI.INSTANCE.getConfig().get("default-minion-limit", 5)
        @JvmField
        val ALLOW_FLOATING_MINIONS = AxMinionsAPI.INSTANCE.getConfig().get("allow-floating-minions", false)
        @JvmField
        val ONLY_OWNER_BREAK = AxMinionsAPI.INSTANCE.getConfig().get("only-owner-break", true)
        @JvmField
        val DISPLAY_WARNINGS = AxMinionsAPI.INSTANCE.getConfig().get("display-warnings", true)
        @JvmField
        val CAN_BREAK_TOOLS = AxMinionsAPI.INSTANCE.getConfig().get("can-break-tools", true)
        @JvmField
        val USE_DURABILITY = AxMinionsAPI.INSTANCE.getConfig().get("use-durability", true)
        @JvmField
        val DATABASE_TYPE = AxMinionsAPI.INSTANCE.getConfig().get("database.type", "H2")
        @JvmField
        val STACKER_HOOK = AxMinionsAPI.INSTANCE.getConfig().get("hooks.stacker", "none")
        @JvmField
        val ECONOMY_HOOK = AxMinionsAPI.INSTANCE.getConfig().get("hooks.economy", "Vault")
        @JvmField
        val PRICES_HOOK = AxMinionsAPI.INSTANCE.getConfig().get("hooks.prices", "ShopGUIPlus")
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