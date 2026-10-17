package com.artillexstudios.axminions.list

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.TimeUtils
import com.artillexstudios.axminions.api.warnings.Warnings
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.InputStream
import java.util.UUID
import com.artillexstudios.axminions.api.config.Config as MinionsConfig

/**
 * The menu opened by /minions list, where a player can see all of their minions.
 */
class MinionList(file: File, stream: InputStream) {
    enum class Sort(val key: String) {
        TYPE("type"),
        LEVEL("level"),
        FUEL("fuel"),
        WORLD("world");

        fun next(): Sort {
            return entries[(ordinal + 1) % entries.size]
        }
    }

    class Holder(val owner: UUID, val ownerName: String) : InventoryHolder {
        var sort = Sort.TYPE
        var page = 0
        private lateinit var inventory: Inventory

        internal fun setInventory(inventory: Inventory) {
            this.inventory = inventory
        }

        override fun getInventory(): Inventory {
            return inventory
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

    private var size = 27
    private var minionSlots = listOf<Int>()
    private var sortSlot = 4
    private var backSlot = 18
    private var nextSlot = 26

    init {
        load()
    }

    fun reload() {
        config.reload()
        load()
    }

    private fun load() {
        size = (config.getInt("size", 27).coerceIn(18, 54) + 8) / 9 * 9
        sortSlot = config.getInt("sort.slot", 4)
        backSlot = config.getInt("back.slot", 18)
        nextSlot = config.getInt("next.slot", 26)

        val slots = linkedSetOf<Int>()
        for (entry in config.getStringList("minion-slots", listOf("9-17"))) {
            val range = entry.split('-', limit = 2)
            val from = range[0].trim().toIntOrNull()
            val to = (range.getOrNull(1) ?: range[0]).trim().toIntOrNull()
            if (from == null || to == null || from > to) {
                AxMinionsPlugin.INSTANCE.logger.warning("list.yml: '$entry' is not a valid slot or slot range, skipping it!")
                continue
            }

            for (slot in from..to) {
                if (slot < 0 || slot >= size) {
                    AxMinionsPlugin.INSTANCE.logger.warning("list.yml: the slot $slot is outside of the menu, skipping it!")
                    continue
                }

                slots.add(slot)
            }
        }

        if (slots.isEmpty()) {
            AxMinionsPlugin.INSTANCE.logger.warning("list.yml: there are no valid minion-slots, using the second row!")
            slots.addAll(9..17)
        }

        minionSlots = slots.toList()
    }

    fun open(player: Player) {
        open(player, player)
    }

    /**
     * Opens the list of the minions of [owner] for [player], which is different from the owner for admins.
     */
    fun open(player: Player, owner: OfflinePlayer) {
        val holder = Holder(owner.uniqueId, owner.name ?: "???")
        if (minionsOf(holder.owner).isEmpty()) {
            sendNoMinions(player, holder)
            return
        }

        val title = if (player.uniqueId == holder.owner) {
            StringUtils.formatToString(config.getString("title", "Your Minions"))
        } else {
            StringUtils.formatToString(
                config.getString("title-other", "<#5555FF><player>'s Minions"),
                Placeholder.unparsed("player", holder.ownerName)
            )
        }

        val inventory = Bukkit.createInventory(holder, size, title)
        holder.setInventory(inventory)

        render(player, holder)
        player.openInventory(inventory)
    }

    private fun sendNoMinions(player: Player, holder: Holder) {
        if (player.uniqueId == holder.owner) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LIST_NO_MINIONS()))
        } else {
            player.sendMessage(
                StringUtils.formatToString(
                    Messages.PREFIX() + Messages.LIST_NO_MINIONS_OTHER(),
                    Placeholder.unparsed("player", holder.ownerName)
                )
            )
        }
    }

    fun click(player: Player, holder: Holder, slot: Int) {
        when (slot) {
            sortSlot -> {
                holder.sort = holder.sort.next()
                holder.page = 0
            }

            backSlot -> {
                if (holder.page == 0) {
                    player.closeInventory()
                    return
                }

                holder.page--
            }

            nextSlot -> holder.page++
            else -> return
        }

        render(player, holder)
    }

    /**
     * Fills the menu of the holder with the current state of the minions, the page gets clamped to the existing ones.
     */
    private fun render(player: Player, holder: Holder) {
        val minions = minionsOf(holder.owner)
        if (minions.isEmpty()) {
            player.closeInventory()
            sendNoMinions(player, holder)
            return
        }

        val now = System.currentTimeMillis()
        val sorted = minions.sortedWith(comparator(holder.sort, now))
        val perPage = minionSlots.size
        val pages = (sorted.size + perPage - 1) / perPage
        holder.page = holder.page.coerceIn(0, pages - 1)

        val inventory = holder.inventory
        val filler = ItemBuilder.create(config.getSection("filler")).get()
        for (i in 0 until inventory.size) {
            inventory.setItem(i, filler)
        }

        sorted.drop(holder.page * perPage).take(perPage).forEachIndexed { index, minion ->
            place(inventory, minionSlots[index], createItem(minion, now))
        }

        val resolvers = arrayOf<TagResolver>(
            Placeholder.unparsed("page", (holder.page + 1).toString()),
            Placeholder.unparsed("pages", pages.toString())
        )

        place(inventory, sortSlot, createSortItem(holder.sort))
        place(inventory, backSlot, ItemBuilder.create(config.getSection("back.item"), *resolvers).clonedGet())
        if (holder.page < pages - 1) {
            place(inventory, nextSlot, ItemBuilder.create(config.getSection("next.item"), *resolvers).clonedGet())
        }
    }

    private fun place(inventory: Inventory, slot: Int, item: ItemStack) {
        // The menu can be smaller than it was when it got opened, if the config got reloaded
        if (slot < inventory.size) {
            inventory.setItem(slot, item)
        }
    }

    private fun minionsOf(owner: UUID): List<Minion> {
        return AxMinionsAPI.INSTANCE.getMinions().filter { it.getOwnerUUID() == owner }
    }

    private fun worldName(minion: Minion): String {
        return minion.getLocation().world?.name ?: "---"
    }

    /**
     * How much fuel the minion has left in milliseconds, or null if the minions don't need any.
     */
    private fun remainingFuel(minion: Minion, now: Long): Long? {
        if (!MinionsConfig.CHARGE_ENABLED()) return null

        return (minion.getCharge() - now).coerceAtLeast(0)
    }

    private fun comparator(sort: Sort, now: Long): Comparator<Minion> {
        val byLocation = compareBy<Minion>(
            { worldName(it) },
            { it.getLocation().blockX },
            { it.getLocation().blockY },
            { it.getLocation().blockZ }
        )
        val byType = compareBy<Minion> { it.getType().getName() }.thenByDescending { it.getLevel() }

        return when (sort) {
            Sort.TYPE -> byType.then(byLocation)
            Sort.LEVEL -> compareByDescending<Minion> { it.getLevel() }.then(byType).then(byLocation)
            Sort.FUEL -> compareByDescending<Minion> { remainingFuel(it, now) ?: 0L }.then(byType).then(byLocation)
            Sort.WORLD -> compareBy<Minion> { worldName(it) }.then(byType).then(byLocation)
        }
    }

    private fun status(minion: Minion, now: Long): String {
        val warning = when {
            !minion.getType().isEnabled() -> return config.getString("status.disabled", "<#FF3333>Disabled")
            remainingFuel(minion, now) == 0L -> Warnings.NO_CHARGE
            else -> minion.getWarning()
        }

        if (warning != null) {
            return config.getString("status.warnings.${warning.getName()}", "<#FF3333>${warning.getName()}")
        }

        if (!minion.isTicking()) {
            return config.getString("status.inactive", "<#AAAAAA>Inactive")
        }

        return config.getString("status.active", "<#33FF33>Active")
    }

    private fun createItem(minion: Minion, now: Long): ItemStack {
        val location = minion.getLocation()
        val fuel = remainingFuel(minion, now)?.let { TimeUtils.format(it) } ?: config.getString("fuel-unlimited", "Unlimited")

        val resolvers = arrayOf<TagResolver>(
            Placeholder.parsed("minion", minion.getType().getConfig().getString("item.name")),
            Placeholder.unparsed("type", minion.getType().getName()),
            Placeholder.unparsed("level", minion.getLevel().toString()),
            Placeholder.parsed("level_color", Messages.LEVEL_COLOR(minion.getLevel())),
            Placeholder.unparsed("world", worldName(minion)),
            Placeholder.unparsed("x", location.blockX.toString()),
            Placeholder.unparsed("y", location.blockY.toString()),
            Placeholder.unparsed("z", location.blockZ.toString()),
            Placeholder.unparsed("fuel", fuel),
            Placeholder.parsed("status", status(minion, now))
        )

        return ItemBuilder.create(minion.getType().getConfig().getSection("item"), *resolvers)
            .setName(config.getString("minion.name", "<minion> Lv.<level>"), *resolvers)
            .setLore(config.getStringList("minion.lore", emptyList()), *resolvers)
            .clonedGet()
    }

    private fun createSortItem(selected: Sort): ItemStack {
        val lore = arrayListOf<String>()
        for (sort in Sort.entries) {
            val line = if (sort == selected) {
                config.getString("sort.selected", " <green>☑ <option>")
            } else {
                config.getString("sort.unselected", " <gray>☐ <option>")
            }

            lore.add(line.replace("<option>", config.getString("sort.options.${sort.key}", sort.key)))
        }

        lore.addAll(config.getStringList("sort.footer", emptyList()))

        return ItemBuilder.create(config.getSection("sort.item")).setLore(lore).clonedGet()
    }
}
