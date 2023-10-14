package com.artillexstudios.axminions.integrations.prices

import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import net.brcdev.shopgui.ShopGuiPlusApi
import org.bukkit.inventory.ItemStack

class ShopGUIPlusIntegration : PricesIntegration {

    override fun getPrice(itemStack: ItemStack): Double {
        return ShopGuiPlusApi.getItemStackPriceSell(itemStack)
    }

    override fun register() {

    }
}