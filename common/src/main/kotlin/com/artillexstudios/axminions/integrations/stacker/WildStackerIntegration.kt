package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axminions.api.integrations.types.StackerIntegration
import com.bgsoftware.wildstacker.api.WildStackerAPI
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

class WildStackerIntegration : StackerIntegration {

    override fun getStackSize(entity: LivingEntity): Long {
        return WildStackerAPI.getStackedEntity(entity)?.stackAmount?.toLong() ?: 1
    }

    override fun getStackSize(item: Item): Long {
        return WildStackerAPI.getStackedItem(item)?.stackAmount?.toLong() ?: 1
    }

    override fun setStackSize(item: Item, amount: Int) {
        val stackedItem = WildStackerAPI.getStackedItem(item)

        if (stackedItem != null) {
            stackedItem.setStackAmount(amount, true)
        } else {
            item.itemStack.amount = amount
        }
    }

    override fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location) {
        WildStackerAPI.getWildStacker().systemManager.spawnItemWithAmount(location, itemStack, amount);
    }

    override fun register() {

    }
}