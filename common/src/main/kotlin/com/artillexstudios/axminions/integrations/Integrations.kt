package com.artillexstudios.axminions.integrations

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.exception.InvalidIntegrationException
import com.artillexstudios.axminions.api.integrations.Integration
import com.artillexstudios.axminions.api.integrations.Integrations
import com.artillexstudios.axminions.api.integrations.types.*
import com.artillexstudios.axminions.integrations.economy.PlayerPointsIntegration
import com.artillexstudios.axminions.integrations.economy.VaultIntegration
import com.artillexstudios.axminions.integrations.placeholder.PlaceholderAPIIntegration
import com.artillexstudios.axminions.integrations.prices.CMIIntegration
import com.artillexstudios.axminions.integrations.prices.EconomyShopGUIIntegration
import com.artillexstudios.axminions.integrations.prices.EssentialsIntegration
import com.artillexstudios.axminions.integrations.prices.ShopGUIPlusIntegration
import com.artillexstudios.axminions.integrations.prices.ZShopIntegration
import com.artillexstudios.axminions.integrations.protection.BentoBoxIntegration
import com.artillexstudios.axminions.integrations.protection.GriefPreventionIntegration
import com.artillexstudios.axminions.integrations.protection.IridiumSkyBlockIntegration
import com.artillexstudios.axminions.integrations.protection.KingdomsXIntegration
import com.artillexstudios.axminions.integrations.protection.LandsIntegration
import com.artillexstudios.axminions.integrations.protection.SuperiorSkyBlock2Integration
import com.artillexstudios.axminions.integrations.protection.TownyIntegration
import com.artillexstudios.axminions.integrations.protection.WorldGuardIntegration
import com.artillexstudios.axminions.integrations.stacker.DefaultStackerIntegration
import com.artillexstudios.axminions.integrations.stacker.RoseStackerIntegration
import com.artillexstudios.axminions.integrations.stacker.WildStackerIntegration
import com.artillexstudios.axminions.listeners.SuperiorSkyBlock2Listener
import java.util.Locale
import org.bukkit.Bukkit

class Integrations : Integrations {
    private lateinit var stackerIntegration: StackerIntegration
    private var pricesIntegration: PricesIntegration? = null
    private var economyIntegration: EconomyIntegration? = null
    private var islandIntegration: IslandIntegration? = null
    private val protectionIntegrations = com.artillexstudios.axminions.integrations.protection.ProtectionIntegrations()
    internal var kGeneratorsIntegration = false
    internal var itemsAdderIntegration = false

    override fun getStackerIntegration(): StackerIntegration {
        return stackerIntegration
    }

    override fun getPricesIntegration(): PricesIntegration? {
        return pricesIntegration
    }

    override fun getEconomyIntegration(): EconomyIntegration? {
        return economyIntegration
    }

    override fun getIslandIntegration(): IslandIntegration? {
        return islandIntegration
    }

    override fun getProtectionIntegration(): ProtectionIntegrations {
        return protectionIntegrations
    }

    override fun reload() {
        when (Config.STACKER_HOOK().lowercase(Locale.ENGLISH)) {
            "rosestacker" -> {
                if (isPluginLoaded("RoseStacker")) {
                    register(RoseStackerIntegration())
                } else {
                    register(DefaultStackerIntegration())
                }
            }

            "wildstacker" -> {
                if (isPluginLoaded("WildStacker")) {
                    register(WildStackerIntegration())
                } else {
                    register(DefaultStackerIntegration())
                }
            }

            else -> {
                register(DefaultStackerIntegration())
            }
        }

        when (Config.PRICES_HOOK().lowercase(Locale.ENGLISH)) {
            "shopguiplus", "shopgui+" -> {
                if (isPluginLoaded("ShopGuiPlus")) {
                    register(ShopGUIPlusIntegration())
                }
            }

            "essentials" -> {
                if (isPluginLoaded("Essentials")) {
                    register(EssentialsIntegration())
                }
            }

            "cmi" -> {
                if (isPluginLoaded("CMI")) {
                    register(CMIIntegration())
                }
            }

            "economyshopgui" -> {
                if (Bukkit.getPluginManager().getPlugin("EconomyShopGUI") != null || Bukkit.getPluginManager().getPlugin("EconomyShopGUI-Premium") != null) {
                    register(EconomyShopGUIIntegration())
                    Bukkit.getConsoleSender()
                        .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into EconomyShopGUI!"))}
            }

            "zshop" -> {
                if (isPluginLoaded("zShop")) {
                    register(ZShopIntegration())
                }
            }
        }

        when (Config.ECONOMY_HOOK().lowercase(Locale.ENGLISH)) {
            "vault" -> {
                if (isPluginLoaded("Vault")) {
                    register(VaultIntegration())
                }
            }

            "playerpoints" -> {
                if (isPluginLoaded("PlayerPoints")) {
                    register(PlayerPointsIntegration())
                }
            }
        }

        protectionIntegrations.clear()

        if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
            register(SuperiorSkyBlock2Integration())
            register(com.artillexstudios.axminions.integrations.island.SuperiorSkyBlock2Integration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into SuperiorSkyblock2!"))
            Bukkit.getPluginManager().registerEvents(SuperiorSkyBlock2Listener(), AxMinionsPlugin.INSTANCE)
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            register(WorldGuardIntegration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into WorldGuard!"))
        }

        if (Bukkit.getPluginManager().getPlugin("BentoBox") != null) {
            register(BentoBoxIntegration())
            register(com.artillexstudios.axminions.integrations.island.BentoBoxIntegration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into BentoBox!"))
        }

        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
            register(GriefPreventionIntegration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into GriefPrevention!"))
        }

        if (Bukkit.getPluginManager().getPlugin("Lands") != null) {
            register(LandsIntegration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into Lands!"))
        }

        if (Bukkit.getPluginManager().getPlugin("IridiumSkyBlock") != null) {
            register(IridiumSkyBlockIntegration())
            register(com.artillexstudios.axminions.integrations.island.IridiumSkyBlockIntegration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into IridiumSkyBlock!"))
        }

        if (Bukkit.getPluginManager().getPlugin("KingdomsX") != null) {
            register(KingdomsXIntegration())
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into KingdomsX!"))
        }

        if (Bukkit.getPluginManager().getPlugin("KGenerators") != null) {
            kGeneratorsIntegration = true
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into KGenerators!"))
        }

        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdderIntegration = true
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into ItemsAdder!"))
        }

        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdderIntegration = true
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into ItemsAdder!"))
        }

        if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
            register(TownyIntegration())
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIIntegration().register()
        }
    }

    override fun register(integration: Integration) {
        when (integration) {
            is StackerIntegration -> {
                stackerIntegration = integration
            }

            is ProtectionIntegration -> {
                protectionIntegrations.register(integration)
            }

            is EconomyIntegration -> {
                economyIntegration = integration
            }

            is PricesIntegration -> {
                pricesIntegration = integration
            }

            is IslandIntegration -> {
                islandIntegration = integration
            }

            else -> {
                throw InvalidIntegrationException("There is no builtin integration that the following class extends: ${integration::class.java}")
            }
        }
        integration.register()
    }

    private fun isPluginLoaded(pluginName: String): Boolean {
        if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] Hooked into $pluginName!"))
            return true
        } else {
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions] <#FF0000>$pluginName is set in the config.yml, but it isn't installed! Setting provider to the default one!"))
            return false
        }
    }

    override fun deregister(integration: Integration) {
        when (integration) {
            is ProtectionIntegration -> {
                protectionIntegrations.deregister(integration)
            }

            else -> {
                throw InvalidIntegrationException("You can only unregister a protection integration!")
            }
        }
    }
}
