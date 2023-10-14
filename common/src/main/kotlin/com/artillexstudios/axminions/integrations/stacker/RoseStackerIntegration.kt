package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axminions.api.integrations.types.StackerIntegration
import dev.rosewood.rosestacker.api.RoseStackerAPI
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

class RoseStackerIntegration : StackerIntegration {
    lateinit var instance: RoseStackerAPI

    override fun getStackSize(entity: LivingEntity): Long {
        return instance.getStackedEntity(entity)?.stackSize?.toLong() ?: 1
    }

    override fun getStackSize(item: Item): Long {
        return instance.getStackedItem(item)?.stackSize?.toLong() ?: 1
    }

    override fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location) {
        instance.dropItemStack(itemStack, amount, location, false)
    }

    override fun register() {
        instance =  RoseStackerAPI.getInstance()
    }
}