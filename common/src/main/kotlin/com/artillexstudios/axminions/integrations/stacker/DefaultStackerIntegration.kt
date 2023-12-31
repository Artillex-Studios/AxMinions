package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.api.integrations.types.StackerIntegration
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

class DefaultStackerIntegration : StackerIntegration {

    override fun getStackSize(entity: LivingEntity): Long {
        return 1
    }

    override fun getStackSize(item: Item): Long {
        return item.itemStack.amount.toLong()
    }

    override fun setStackSize(item: Item, amount: Int) {
        item.itemStack.amount = amount
    }

    override fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location) {
        Scheduler.get().executeAt(location) {
            location.world!!.dropItem(location, itemStack)
        }
    }

    override fun register() {

    }
}