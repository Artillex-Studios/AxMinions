package com.artillexstudios.axminions.integrations.prices

import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import fr.maxlego08.zshop.api.ShopManager
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

class ZShopIntegration : PricesIntegration {
    private var shopManager: ShopManager? = null

    override fun getPrice(itemStack: ItemStack): Double {
        val button = shopManager?.getItemButton(itemStack.type)
        return button?.map { itemButton -> itemButton.getSellPrice(itemStack.amount) }?.orElse(0.0) ?: 0.0
    }

    override fun register() {
        val rsp = Bukkit.getServer().servicesManager.getRegistration(
            ShopManager::class.java
        )
        shopManager = rsp!!.provider
    }
}