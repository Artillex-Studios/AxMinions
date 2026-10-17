package com.artillexstudios.axminions.shop

import net.milkbowl.vault.economy.Economy
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI
import java.util.Locale
import kotlin.math.ceil

/**
 * A currency the minion shop can charge in. Unlike the global economy hook, every shop entry picks its own currency,
 * so all of these can be used side by side. Implementations must only be instantiated when their plugin is installed
 * (see [ShopCurrencies]), otherwise their classes can't be linked.
 */
interface ShopCurrency {
    fun isAvailable(currencyId: String): Boolean

    fun getBalance(player: Player, currencyId: String): Double

    /**
     * Takes [amount] from the player, returns false (and takes nothing) if that wasn't possible.
     */
    fun withdraw(player: Player, currencyId: String, amount: Double): Boolean
}

object ShopCurrencies {
    private val cache = hashMapOf<String, ShopCurrency>()

    /**
     * Returns the currency registered under [name] (vault, playerpoints, excellenteconomy),
     * or null if the name is unknown or the plugin providing it isn't installed.
     */
    fun get(name: String): ShopCurrency? {
        val key = name.lowercase(Locale.ENGLISH)
        cache[key]?.let { return it }

        val currency = when (key) {
            "vault" -> if (isPluginEnabled("Vault")) VaultShopCurrency() else null
            "playerpoints" -> if (isPluginEnabled("PlayerPoints")) PlayerPointsShopCurrency() else null
            "excellenteconomy" -> if (isPluginEnabled("ExcellentEconomy")) ExcellentEconomyShopCurrency() else null
            else -> null
        } ?: return null

        cache[key] = currency
        return currency
    }

    fun isKnown(name: String): Boolean {
        return name.lowercase(Locale.ENGLISH) in arrayOf("vault", "playerpoints", "excellenteconomy")
    }

    private fun isPluginEnabled(name: String): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled(name)
    }
}

private class VaultShopCurrency : ShopCurrency {
    private fun economy(): Economy? {
        return Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
    }

    override fun isAvailable(currencyId: String): Boolean {
        return economy() != null
    }

    override fun getBalance(player: Player, currencyId: String): Double {
        return economy()?.getBalance(player) ?: 0.0
    }

    override fun withdraw(player: Player, currencyId: String, amount: Double): Boolean {
        val economy = economy() ?: return false
        if (!economy.has(player, amount)) return false

        return economy.withdrawPlayer(player, amount).transactionSuccess()
    }
}

private class PlayerPointsShopCurrency : ShopCurrency {
    override fun isAvailable(currencyId: String): Boolean {
        return PlayerPoints.getInstance() != null
    }

    override fun getBalance(player: Player, currencyId: String): Double {
        return PlayerPoints.getInstance().api.look(player.uniqueId).toDouble()
    }

    override fun withdraw(player: Player, currencyId: String, amount: Double): Boolean {
        // Points are whole numbers, never charge less than what the price says
        val points = ceil(amount).toInt()
        val api = PlayerPoints.getInstance().api
        if (api.look(player.uniqueId) < points) return false

        return api.take(player.uniqueId, points)
    }
}

private class ExcellentEconomyShopCurrency : ShopCurrency {
    private fun api(): ExcellentEconomyAPI? {
        return Bukkit.getServicesManager().getRegistration(ExcellentEconomyAPI::class.java)?.provider
    }

    override fun isAvailable(currencyId: String): Boolean {
        return api()?.hasCurrency(currencyId) ?: false
    }

    override fun getBalance(player: Player, currencyId: String): Double {
        return api()?.getBalance(player, currencyId) ?: 0.0
    }

    override fun withdraw(player: Player, currencyId: String, amount: Double): Boolean {
        val api = api() ?: return false
        if (!api.hasCurrency(currencyId) || api.getBalance(player, currencyId) < amount) return false

        return api.withdraw(player, currencyId, amount)
    }
}
