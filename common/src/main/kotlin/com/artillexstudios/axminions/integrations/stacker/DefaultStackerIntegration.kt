package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axminions.AxMinionsPlugin
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

    override fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location) {
        location.world!!.dropItem(location, itemStack)
    }

    override fun register() {
        AxMinionsPlugin.integrations.register(this)
    }
}