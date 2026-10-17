package com.artillexstudios.axminions.shop

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.NumberFormatUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.io.File
import java.io.InputStream

/**
 * The menu opened by /minions shop, where every minion type can be bought for its own price and currency.
 */
class MinionShop(file: File, stream: InputStream) {
    class Entry(
        val type: MinionType,
        val slot: Int,
        val currency: ShopCurrency?,
        val currencyId: String,
        val currencyName: String,
        val price: Double
    )

    class Holder(val entries: Map<Int, Entry>) : InventoryHolder {
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

    private var size = 36
    private var entries = listOf<Entry>()

    init {
        load()
    }

    fun reload() {
        config.reload()
        load()
    }

    private fun load() {
        size = (config.getInt("size", 36).coerceIn(9, 54) + 8) / 9 * 9

        val loaded = arrayListOf<Entry>()
        val section = config.getSection("minions")
        if (section != null) {
            for (key in section.getRoutesAsStrings(false)) {
                val type = MinionTypes.valueOf(key)
                if (type == null) {
                    AxMinionsPlugin.INSTANCE.logger.warning("shop.yml: unknown minion type '$key', skipping it!")
                    continue
                }

                val slot = config.getInt("minions.$key.slot", -1)
                if (slot < 0 || slot >= size) {
                    AxMinionsPlugin.INSTANCE.logger.warning("shop.yml: the slot of '$key' is outside of the menu, skipping it!")
                    continue
                }

                val currencyKey = config.getString("minions.$key.currency", "vault").lowercase()
                if (!ShopCurrencies.isKnown(currencyKey)) {
                    AxMinionsPlugin.INSTANCE.logger.warning("shop.yml: unknown currency '$currencyKey' for '$key', skipping it! (vault, playerpoints, excellenteconomy)")
                    continue
                }

                loaded.add(
                    Entry(
                        type,
                        slot,
                        ShopCurrencies.get(currencyKey),
                        config.getString("minions.$key.currency-id", com.artillexstudios.axminions.api.config.Config.ECONOMY_CURRENCY()),
                        config.getString("minions.$key.currency-name", config.getString("currency-names.$currencyKey", currencyKey)),
                        config.getDouble("minions.$key.price", 0.0)
                    )
                )
            }
        }

        entries = loaded
    }

    fun open(player: Player) {
        val available = entries.filter { it.type.isEnabled() }
        val holder = Holder(available.associateBy { it.slot })
        val inventory = Bukkit.createInventory(holder, size, StringUtils.formatToString(config.getString("title", "Minion Shop")))
        holder.setInventory(inventory)

        val filler = ItemBuilder.create(config.getSection("filler")).get()
        for (i in 0 until size) {
            inventory.setItem(i, filler)
        }

        available.forEach {
            inventory.setItem(it.slot, createItem(player, it))
        }

        player.openInventory(inventory)
    }

    fun buy(player: Player, entry: Entry) {
        val resolvers = arrayOf(
            Placeholder.parsed("minion", entry.type.getConfig().getString("item.name")),
            Placeholder.unparsed("price", NumberFormatUtils.format(entry.price)),
            Placeholder.parsed("currency", entry.currencyName)
        )

        // The type got disabled while the shop was open
        if (!entry.type.isEnabled()) {
            player.closeInventory()
            player.sendMessage(
                StringUtils.formatToString(
                    Messages.PREFIX() + Messages.MINION_TYPE_DISABLED(),
                    Placeholder.unparsed("type", entry.type.getName())
                )
            )
            return
        }

        val currency = entry.currency
        if (currency == null || !currency.isAvailable(entry.currencyId)) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SHOP_CURRENCY_UNAVAILABLE(), *resolvers))
            return
        }

        if (currency.getBalance(player, entry.currencyId) < entry.price) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SHOP_NOT_ENOUGH_MONEY(), *resolvers))
            return
        }

        val item = entry.type.getItem()
        if (!hasSpace(player.inventory, item)) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.ERROR_INVENTORY_FULL()))
            return
        }

        if (entry.price > 0 && !currency.withdraw(player, entry.currencyId, entry.price)) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SHOP_NOT_ENOUGH_MONEY(), *resolvers))
            return
        }

        player.inventory.addItem(item).values.forEach {
            player.world.dropItemNaturally(player.location, it)
        }

        player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SHOP_BOUGHT(), *resolvers))
    }

    /**
     * The minion's own item, with the lines from the 'lore' option of the shop appended to its lore.
     */
    private fun createItem(player: Player, entry: Entry): ItemStack {
        val currency = entry.currency
        val balance = if (currency != null && currency.isAvailable(entry.currencyId)) {
            NumberFormatUtils.format(currency.getBalance(player, entry.currencyId))
        } else {
            "---"
        }

        val resolvers: Array<TagResolver> = arrayOf(
            Placeholder.unparsed("level", "1"),
            Placeholder.unparsed("actions", "0"),
            Placeholder.unparsed("price", NumberFormatUtils.format(entry.price)),
            Placeholder.parsed("currency", entry.currencyName),
            Placeholder.unparsed("balance", balance)
        )

        val minionConfig = entry.type.getConfig()
        val lore = minionConfig.getStringList("item.lore", emptyList()) + config.getStringList("lore", emptyList())

        return ItemBuilder.create(minionConfig.getSection("item"), *resolvers).setLore(lore, *resolvers).clonedGet()
    }

    private fun hasSpace(inventory: PlayerInventory, item: ItemStack): Boolean {
        if (inventory.firstEmpty() != -1) return true

        return inventory.storageContents.any { it != null && it.isSimilar(item) && it.amount < it.maxStackSize }
    }
}
