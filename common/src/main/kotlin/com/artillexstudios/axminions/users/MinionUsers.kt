package com.artillexstudios.axminions.users

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
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.utils.HeadTextures
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * The players an owner trusts, and the menu opened by /minions users where they can be managed.
 * A trusted player can use every minion of the owner, see [Minion.hasFullAccess] for what is still left to the owner.
 */
class MinionUsers(file: File, stream: InputStream) {
    class Holder(val owner: UUID) : InventoryHolder {
        var page = 0

        // The trusted player shown in a slot, filled by the last render
        internal val shown = HashMap<Int, UUID>()
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

    // Read by the minions on every click, so this has to stay in memory
    private val trusted = ConcurrentHashMap<UUID, MutableSet<UUID>>()

    // The players who are about to type the name of a new trusted player in the chat, with the time the prompt runs out
    private val prompts = ConcurrentHashMap<UUID, Long>()

    private var size = 27
    private var userSlots = listOf<Int>()
    private var addSlot = 4
    private var backSlot = 18
    private var nextSlot = 26

    init {
        load()

        for ((owner, users) in AxMinionsPlugin.dataHandler.loadTrustedUsers()) {
            trusted[owner] = ConcurrentHashMap.newKeySet<UUID>().also { it.addAll(users) }
        }
    }

    fun reload() {
        config.reload()
        load()
    }

    private fun load() {
        size = (config.getInt("size", 27).coerceIn(18, 54) + 8) / 9 * 9
        addSlot = config.getInt("add.slot", 4)
        backSlot = config.getInt("back.slot", 18)
        nextSlot = config.getInt("next.slot", 26)

        val slots = linkedSetOf<Int>()
        for (entry in config.getStringList("user-slots", listOf("9-17"))) {
            val range = entry.split('-', limit = 2)
            val from = range[0].trim().toIntOrNull()
            val to = (range.getOrNull(1) ?: range[0]).trim().toIntOrNull()
            if (from == null || to == null || from > to) {
                AxMinionsPlugin.INSTANCE.logger.warning("users.yml: '$entry' is not a valid slot or slot range, skipping it!")
                continue
            }

            for (slot in from..to) {
                if (slot < 0 || slot >= size) {
                    AxMinionsPlugin.INSTANCE.logger.warning("users.yml: the slot $slot is outside of the menu, skipping it!")
                    continue
                }

                slots.add(slot)
            }
        }

        if (slots.isEmpty()) {
            AxMinionsPlugin.INSTANCE.logger.warning("users.yml: there are no valid user-slots, using the second row!")
            slots.addAll(9..17)
        }

        userSlots = slots.toList()
    }

    fun isTrusted(owner: UUID, player: UUID): Boolean {
        return trusted[owner]?.contains(player) == true
    }

    fun getTrusted(owner: UUID): List<UUID> {
        return trusted[owner]?.toList() ?: emptyList()
    }

    fun open(player: Player) {
        val holder = Holder(player.uniqueId)
        val inventory = Bukkit.createInventory(holder, size, StringUtils.formatToString(config.getString("title", "Trusted Players")))
        holder.setInventory(inventory)

        render(holder)
        player.openInventory(inventory)
    }

    fun click(player: Player, holder: Holder, slot: Int) {
        when (slot) {
            addSlot -> {
                startPrompt(player)
                return
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
                val user = holder.shown[slot] ?: return
                remove(holder.owner, user)
            }
        }

        render(holder)
    }

    private fun startPrompt(player: Player) {
        val timeout = config.getInt("add.timeout", 30).coerceAtLeast(1)
        prompts[player.uniqueId] = System.currentTimeMillis() + timeout * 1000L

        player.closeInventory()
        player.sendMessage(
            StringUtils.formatToString(
                Messages.PREFIX() + Messages.USERS_PROMPT(),
                Placeholder.unparsed("seconds", timeout.toString())
            )
        )
    }

    fun cancelPrompt(player: UUID) {
        prompts.remove(player)
    }

    /**
     * Gets the chat messages of players with a running prompt, returns true if the message was used as one.
     * Runs on the thread of the chat, which is not the one of the player.
     */
    fun handleChat(player: Player, message: String): Boolean {
        val expires = prompts.remove(player.uniqueId) ?: return false
        // The player forgot about the prompt, so this is just a normal message
        if (System.currentTimeMillis() > expires) return false

        val name = message.trim()
        if (!name.equals("cancel", true)) {
            add(player, name)
        } else {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.USERS_CANCELLED()))
        }

        // The menu belongs to the thread of the player
        Scheduler.get().run(player, { open(player) }, {})
        return true
    }

    private fun add(player: Player, name: String) {
        val target = find(name)
        if (target == null) {
            player.sendMessage(
                StringUtils.formatToString(
                    Messages.PREFIX() + Messages.USERS_NOT_FOUND(),
                    Placeholder.unparsed("player", name)
                )
            )
            return
        }

        val targetName = target.name ?: name
        val resolver = Placeholder.unparsed("player", targetName)

        if (target.uniqueId == player.uniqueId) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.USERS_SELF(), resolver))
            return
        }

        if (!trusted.getOrPut(player.uniqueId) { ConcurrentHashMap.newKeySet() }.add(target.uniqueId)) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.USERS_ALREADY_TRUSTED(), resolver))
            return
        }

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.addTrustedUser(player.uniqueId, target.uniqueId)
        }

        player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.USERS_ADDED(), resolver))
    }

    private fun remove(owner: UUID, user: UUID) {
        val users = trusted[owner] ?: return
        if (!users.remove(user)) return

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.removeTrustedUser(owner, user)
        }

        val name = Bukkit.getOfflinePlayer(user).name ?: "???"
        Bukkit.getPlayer(owner)?.sendMessage(
            StringUtils.formatToString(
                Messages.PREFIX() + Messages.USERS_REMOVED(),
                Placeholder.unparsed("player", name)
            )
        )

        // Whoever is using a minion of the owner only because of the trust has to stop now
        val removed = Bukkit.getPlayer(user) ?: return
        Scheduler.get().run(removed, {
            val holder = removed.openInventory.topInventory.holder
            if (holder is Minion && holder.getOwnerUUID() == owner && !holder.hasFullAccess(removed)) {
                removed.closeInventory()
            }
        }, {})
    }

    /**
     * Finds the player by the name they typed, only players who joined the server before are valid.
     * This can ask the profile cache or the servers of Mojang, so it must never run on the main thread.
     */
    private fun find(name: String): OfflinePlayer? {
        Bukkit.getPlayerExact(name)?.let { return it }

        @Suppress("DEPRECATION")
        val offline = Bukkit.getOfflinePlayer(name)
        return if (offline.hasPlayedBefore()) offline else null
    }

    /**
     * Fills the menu of the holder with the current trusted players, the page gets clamped to the existing ones.
     */
    private fun render(holder: Holder) {
        val users = getTrusted(holder.owner)
            .map { it to (Bukkit.getOfflinePlayer(it).name ?: "???") }
            .sortedBy { it.second.lowercase() }

        val perPage = userSlots.size
        val pages = ((users.size + perPage - 1) / perPage).coerceAtLeast(1)
        holder.page = holder.page.coerceIn(0, pages - 1)

        val inventory = holder.inventory
        val filler = HeadTextures.item(config.getSection("filler")).get()
        for (i in 0 until inventory.size) {
            inventory.setItem(i, filler)
        }

        holder.shown.clear()
        users.drop(holder.page * perPage).take(perPage).forEachIndexed { index, (uuid, name) ->
            val slot = userSlots[index]
            // The menu can be smaller than it was when it got opened, if the config got reloaded
            if (slot < inventory.size) {
                holder.shown[slot] = uuid
                inventory.setItem(slot, createHead(uuid, name))
            }
        }

        val resolvers = arrayOf<TagResolver>(
            Placeholder.unparsed("page", (holder.page + 1).toString()),
            Placeholder.unparsed("pages", pages.toString())
        )

        place(inventory, addSlot, HeadTextures.item(config.getSection("add.item")).clonedGet())
        place(inventory, backSlot, HeadTextures.item(config.getSection("back.item"), *resolvers).clonedGet())
        if (holder.page < pages - 1) {
            place(inventory, nextSlot, HeadTextures.item(config.getSection("next.item"), *resolvers).clonedGet())
        }
    }

    private fun place(inventory: Inventory, slot: Int, item: ItemStack) {
        if (slot < inventory.size) {
            inventory.setItem(slot, item)
        }
    }

    private fun createHead(uuid: UUID, name: String): ItemStack {
        val status = if (Bukkit.getPlayer(uuid) != null) {
            config.getString("status.online", "<#33FF33>Online")
        } else {
            config.getString("status.offline", "<#AAAAAA>Offline")
        }

        val resolvers = arrayOf<TagResolver>(
            Placeholder.unparsed("player", name),
            Placeholder.parsed("status", status)
        )

        val item = ItemBuilder.create(Material.PLAYER_HEAD)
            .setName(config.getString("user.name", "<player>"), *resolvers)
            .setLore(config.getStringList("user.lore", emptyList()), *resolvers)
            .clonedGet()

        val meta = item.itemMeta as? SkullMeta ?: return item
        meta.owningPlayer = Bukkit.getOfflinePlayer(uuid)
        item.itemMeta = meta
        return item
    }
}
