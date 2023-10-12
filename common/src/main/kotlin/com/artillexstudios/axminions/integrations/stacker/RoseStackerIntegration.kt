package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axminions.api.integrations.types.StackerIntegration
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

class RoseStackerIntegration : StackerIntegration {
    override fun getStackSize(entity: LivingEntity): Long {
        TODO("Not yet implemented")
    }

    override fun getStackSize(item: Item): Long {
        TODO("Not yet implemented")
    }

    override fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location) {
        TODO("Not yet implemented")
    }

    override fun register() {
        TODO("Not yet implemented")
    }
}