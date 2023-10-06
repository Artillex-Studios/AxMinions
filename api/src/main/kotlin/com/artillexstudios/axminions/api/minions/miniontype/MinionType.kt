package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.io.InputStream

abstract class MinionType(private val name: String, private val defaults: InputStream) {
    private lateinit var config: Config

    fun load() {
        config = Config(File(AxMinionsAPI.INSTANCE.getAxMinionsDataFolder(), "/minions/$name.yml"), defaults)
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

    fun updateArmor(minion: Minion) {

    }

    fun getItem(level: Int = 1): ItemStack {
        val builder = ItemBuilder(config.getSection("item"))
        builder.storePersistentData(MinionTypes.getMinionKey(), PersistentDataType.STRING, name)
        builder.storePersistentData(MinionTypes.getLevelKey(), PersistentDataType.INTEGER, level)

        return builder.clonedGet()
    }

    fun getConfig(): Config {
        return this.config
    }

    fun getString(key: String, level: Int): String {
        return get(key, level, "---", String::class.java)!!
    }

    fun getDouble(key: String, level: Int): Double {
        return get(key, level, -1.0, Double::class.java)!!
    }

    fun getLong(key: String, level: Int): Long {
        return get(key, level, -1, Long::class.java)!!
    }

    fun getSection(key: String, level: Int): Section? {
        return get(key, level, null, Section::class.java)
    }

    private fun <T> get(key: String, level: Int, defaultValue: T?, clazz: Class<T>): T? {
        var n = defaultValue

        config.getSection("upgrades").getRoutesAsStrings(false).forEach {
            if (it.toInt() > level) return n

            if (config.backingDocument.getAsOptional("upgrades.$it.$key", clazz).isEmpty) return@forEach

            n = config.get("upgrades.$it.$key")
        }

        return n
    }

    abstract fun run(minion: Minion)
}