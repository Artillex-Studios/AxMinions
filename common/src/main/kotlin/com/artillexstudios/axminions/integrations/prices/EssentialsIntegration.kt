package com.artillexstudios.axminions.integrations.prices

import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import com.earth2me.essentials.IEssentials
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

class EssentialsIntegration : PricesIntegration {
    private lateinit var manager: IEssentials;

    override fun getPrice(itemStack: ItemStack): Double {
        val price = manager.worth.getPrice(manager, itemStack)
        if (price == null) {
            return 0.0
        }

        return price.toDouble() * itemStack.amount
    }

    override fun register() {
        manager = Bukkit.getPluginManager().getPlugin("Essentials") as IEssentials
    }
}