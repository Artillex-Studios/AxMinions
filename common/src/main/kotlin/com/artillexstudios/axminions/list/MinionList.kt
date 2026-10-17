package com.artillexstudios.axminions.list

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.NumberFormatUtils
import com.artillexstudios.axminions.api.utils.TimeUtils
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.shop.ShopCurrencies
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
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

        // The minion in every slot of the menu right now, so a click always hits the minion that was shown
        internal val shown = hashMapOf<Int, Minion>()
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

    private var teleportEnabled = true
    private var teleportPrice = 0.0
    private var teleportCurrency = "vault"
    private var teleportCurrencyId = ""
    private var teleportCurrencyName = "$"

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
        loadTeleport()

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

    private fun loadTeleport() {
        teleportEnabled = config.getBoolean("teleport.enabled", true)
        teleportPrice = if (config.getBoolean("teleport.cost.enabled", false)) config.getDouble("teleport.cost.price", 0.0) else 0.0
        teleportCurrency = config.getString("teleport.cost.currency", "vault").lowercase()
        teleportCurrencyId = config.getString("teleport.cost.currency-id", MinionsConfig.ECONOMY_CURRENCY())
        teleportCurrencyName = config.getString("teleport.cost.currency-name", "$")

        // Better no teleporting than a free one, because of a typo
        if (teleportEnabled && teleportPrice > 0 && !ShopCurrencies.isKnown(teleportCurrency)) {
            AxMinionsPlugin.INSTANCE.logger.warning("list.yml: unknown currency '$teleportCurrency' for the teleport cost, teleporting is disabled! (vault, playerpoints, excellenteconomy)")
            teleportEnabled = false
        }
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

    fun click(player: Player, holder: Holder, slot: Int, click: ClickType) {
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

            else -> {
                if (click == ClickType.SHIFT_RIGHT) {
                    holder.shown[slot]?.let { teleport(player, holder, it) }
                }
                return
            }
        }

        render(player, holder)
    }

    /**
     * Whether teleporting costs something for [player], admins looking at the minions of someone else never pay.
     */
    private fun teleportCosts(player: Player, holder: Holder): Boolean {
        return teleportPrice > 0 && player.uniqueId == holder.owner
    }

    private fun teleport(player: Player, holder: Holder, minion: Minion) {
        if (!teleportEnabled) return

        // The minion got picked up while the menu was open
        if (minion !in AxMinionsAPI.INSTANCE.getMinions()) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LIST_TELEPORT_MINION_GONE()))
            render(player, holder)
            return
        }

        // The location of the minion is the one it stores, so only ever hand out copies of it
        val target = minion.getLocation().clone()
        if (target.world == null) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LIST_TELEPORT_WORLD_UNAVAILABLE()))
            return
        }

        val resolvers = arrayOf<TagResolver>(
            Placeholder.parsed("minion", minion.getType().getConfig().getString("item.name")),
            Placeholder.unparsed("price", NumberFormatUtils.format(teleportPrice)),
            Placeholder.parsed("currency", teleportCurrencyName)
        )

        val costs = teleportCosts(player, holder)
        val currency = if (costs) ShopCurrencies.get(teleportCurrency) else null
        if (costs) {
            if (currency == null || !currency.isAvailable(teleportCurrencyId)) {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LIST_TELEPORT_CURRENCY_UNAVAILABLE(), *resolvers))
                return
            }

            if (currency.getBalance(player, teleportCurrencyId) < teleportPrice) {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LIST_TELEPORT_NOT_ENOUGH_MONEY(), *resolvers))
                return
            }
        }

        // The blocks next to the minion can only be looked at from the thread that owns them
        Scheduler.get().executeAt(target) {
            val spot = MinionTeleport.findSpot(target)
            if (spot == null) {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LIST_TELEPORT_NO_SAFE_LOCATION(), *resolvers))
                return@executeAt
            }

            // The player pays after they got teleported, another plugin can cancel the teleport.
            // The balance got checked above, so the withdrawal only fails if they spent it in the meantime.
            MinionTeleport.teleport(player, spot) {
                val paid = currency != null && currency.withdraw(player, teleportCurrencyId, teleportPrice)
                val message = if (paid) Messages.LIST_TELEPORT_SUCCESS_PAID() else Messages.LIST_TELEPORT_SUCCESS()

                Scheduler.get().run(player, {
                    player.closeInventory()
                    player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + message, *resolvers))
                }, {})
            }
        }
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

        val costs = teleportCosts(player, holder)
        holder.shown.clear()
        sorted.drop(holder.page * perPage).take(perPage).forEachIndexed { index, minion ->
            holder.shown[minionSlots[index]] = minion
            place(inventory, minionSlots[index], createItem(minion, now, costs))
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
            !minion.isWorking() -> return config.getString("status.stopped", "<#FF9900>Stopped")
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

    private fun createItem(minion: Minion, now: Long, teleportCosts: Boolean): ItemStack {
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
            Placeholder.parsed("status", status(minion, now)),
            Placeholder.unparsed("price", NumberFormatUtils.format(teleportPrice)),
            Placeholder.parsed("currency", teleportCurrencyName)
        )

        val lore = arrayListOf<String>()
        lore.addAll(config.getStringList("minion.lore", emptyList()))
        if (teleportEnabled) {
            lore.addAll(config.getStringList(if (teleportCosts) "teleport.lore-paid" else "teleport.lore", emptyList()))
        }

        return ItemBuilder.create(minion.getType().getConfig().getSection("item"), *resolvers)
            .setName(config.getString("minion.name", "<minion> Lv.<level>"), *resolvers)
            .setLore(lore, *resolvers)
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
