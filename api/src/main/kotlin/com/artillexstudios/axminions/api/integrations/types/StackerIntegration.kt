package com.artillexstudios.axminions.api.integrations.types

import com.artillexstudios.axminions.api.integrations.Integration
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

interface StackerIntegration : Integration {

    fun getStackSize(entity: LivingEntity): Long

    fun getStackSize(item: Item): Long

    fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location)
}