package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.Location
import java.io.File
import java.io.InputStream

abstract class MinionType(private val name: String, private val defaults: InputStream) {
    private lateinit var config: Config

    fun load() {
        config = Config(File(AxMinionsAPI.INSTANCE.getAxMinionsDataFolder(), "$name.yml"), defaults)
        AxMinionsAPI.INSTANCE.getDataHandler().loadMinionsOfType(this)
    }

    fun getName(): String {
        return this.name
    }

    open fun shouldRun(minion: Minion): Boolean {
        return true
    }

    private fun isChunkLoaded(location: Location): Boolean {
        return location.world?.isChunkLoaded(location.blockX shr 4, location.blockZ shr 4) ?: return false
    }

    fun tick(minion: Minion) {
        if (!isChunkLoaded(minion.getLocation())) return
        if (!shouldRun(minion)) return

        run(minion)
    }

    fun getConfig(): Config {
        return this.config
    }

    abstract fun run(minion: Minion)
}