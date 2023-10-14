package com.artillexstudios.axminions.integrations.prices

import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import me.gypopo.economyshopgui.api.EconomyShopGUIHook
import org.bukkit.inventory.ItemStack

class EconomyShopGUIIntegration : PricesIntegration {

    override fun getPrice(itemStack: ItemStack): Double {
        val item = EconomyShopGUIHook.getShopItem(itemStack) ?: return 0.0
        return EconomyShopGUIHook.getItemSellPrice(item, itemStack) ?: 0.0
    }

    override fun register() {

    }
}