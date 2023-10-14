package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.libs.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.Keys
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.io.InputStream

abstract class MinionType(private val name: String, private val defaults: InputStream) {
    private lateinit var config: Config

    fun load() {
        config = Config(File(AxMinionsAPI.INSTANCE.getAxMinionsDataFolder(), "/minions/$name.yml"), defaults)
        AxMinionsAPI.INSTANCE.getDataHandler().insertType(this)
        AxMinionsAPI.INSTANCE.getDataHandler().loadMinionsOfType(this)
    }

    fun getName(): String {
        return this.name
    }

    open fun onToolDirty(minion: Minion) {

    }

    open fun shouldRun(minion: Minion): Boolean {
        return true
    }

    fun isTicking(minion: Minion): Boolean {
        return minion.isTicking()
    }

    fun tick(minion: Minion) {
        if (!minion.isTicking()) return
        if (!shouldRun(minion)) return

        minion.resetAnimation()
        run(minion)
    }

    fun getItem(level: Int = 1, actions: Long = 0): ItemStack {
        val builder = ItemBuilder(config.getSection("item"), Placeholder.unparsed("level", level.toString()), Placeholder.unparsed("actions", actions.toString()))
        builder.storePersistentData(Keys.MINION_TYPE, PersistentDataType.STRING, name)
        builder.storePersistentData(Keys.LEVEL, PersistentDataType.INTEGER, level)

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

    fun hasReachedMaxLevel(minion: Minion): Boolean {
        return !config.backingDocument.isSection("upgrades.${minion.getLevel() + 1}")
    }

    abstract fun run(minion: Minion)
}