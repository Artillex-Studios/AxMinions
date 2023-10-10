package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.inventory.ItemStack

interface PricesIntegration : Integration {

    fun getPrice(itemStack: ItemStack): Double
}