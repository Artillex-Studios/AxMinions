package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.Keys
import java.io.File
import java.io.InputStream
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class MinionType(private val name: String, private val defaults: InputStream) {
    private lateinit var config: Config

    fun load() {
        config = Config(
            File(AxMinionsAPI.INSTANCE.getAxMinionsDataFolder(), "/minions/$name.yml"),
            defaults,
            GeneralSettings.builder().setUseDefaults(false).build(),
            LoaderSettings.DEFAULT,
            DumperSettings.DEFAULT,
            UpdaterSettings.DEFAULT
        )
        AxMinionsAPI.INSTANCE.getDataHandler().insertType(this)
    }

    fun getName(): String {
        return this.name
    }

    open fun onToolDirty(minion: Minion) {

    }

    open fun shouldRun(minion: Minion): Boolean {
        return true
    }

    fun tick(minion: Minion) {
        if (!com.artillexstudios.axminions.api.config.Config.WORK_WHEN_OWNER_OFFLINE() && !minion.isOwnerOnline()) return
        if (!shouldRun(minion)) return

        minion.resetAnimation()
        run(minion)
    }

    fun getItem(level: Int = 1, actions: Long = 0, charge: Long = 0): ItemStack {
        val builder = ItemBuilder(
            config.getSection("item"),
            Placeholder.unparsed("level", level.toString()),
            Placeholder.unparsed("actions", actions.toString())
        )
        val item = builder.clonedGet()
        val meta = item.itemMeta!!
        meta.persistentDataContainer.set(Keys.MINION_TYPE, PersistentDataType.STRING, name)
        meta.persistentDataContainer.set(Keys.LEVEL, PersistentDataType.INTEGER, level)
        meta.persistentDataContainer.set(Keys.STATISTICS, PersistentDataType.LONG, actions)
        meta.persistentDataContainer.set(Keys.CHARGE, PersistentDataType.LONG, charge)
        item.itemMeta = meta
        return item
    }

    fun getConfig(): Config {
        return this.config
    }

    fun getString(key: String, level: Int): String {
        return get(key, level, "---", String::class.java)!!
    }

    fun getDouble(key: String, level: Int): Double {
        return get(key, level, 0.0, Double::class.java)!!
    }

    fun getLong(key: String, level: Int): Long {
        return get(key, level, 0, Long::class.java)!!
    }

    fun getSection(key: String, level: Int): Section? {
        return get(key, level, null, Section::class.java)
    }

    private fun <T> get(key: String, level: Int, defaultValue: T?, clazz: Class<T>): T? {
        var n = defaultValue

        config.getSection("upgrades").getRoutesAsStrings(false).forEach {
            if (it.toInt() > level) {
                return n
            }

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