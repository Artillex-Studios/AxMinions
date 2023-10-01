package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import java.io.File
import java.io.InputStream

class Messages(file: File, stream: InputStream) {
    companion object {
        @JvmField
        var PREFIX = AxMinionsAPI.INSTANCE.getMessages().get<String>("prefix")
        @JvmField
        var NO_CONTAINER_WARNING = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-container")
        @JvmField
        var NO_TOOL_WARNING = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-tool")
        @JvmField
        var NO_WATER_NEARBY_WARNING = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-water-nearby")
        @JvmField
        var CONTAINER_FULL_WARNING = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.container-full")
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

    fun getFormatted(route: String?): String {
        return StringUtils.formatToString(this.config.get(route))
    }

    fun reload() {
        config.reload()
    }
}