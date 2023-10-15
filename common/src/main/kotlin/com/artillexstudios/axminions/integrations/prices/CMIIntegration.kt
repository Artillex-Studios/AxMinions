package com.artillexstudios.axminions.integrations.prices

import com.Zrips.CMI.CMI
import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import org.bukkit.inventory.ItemStack


class CMIIntegration : PricesIntegration {
    private lateinit var manager: CMI


    override fun getPrice(itemStack: ItemStack): Double {
        val worth = manager.worthManager.getWorth(itemStack) ?: return -1.0

        return if (worth.sellPrice == 0.0) -1.0 else worth.sellPrice
    }

    override fun register() {
        manager = CMI.getInstance();
    }
}