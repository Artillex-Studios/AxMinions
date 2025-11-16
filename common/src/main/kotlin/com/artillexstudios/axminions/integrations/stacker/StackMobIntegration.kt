package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.api.integrations.types.StackerIntegration
import uk.antiperson.stackmob.StackMob
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

class StackMobIntegration : StackerIntegration {
    lateinit var instance: StackMob

    override fun getStackSize(entity: LivingEntity): Long {
        if (!instance.getEntityManager().isStackedEntity(entity)) return 1L

        return instance.getEntityManager().getStackEntity(entity).getSize().toLong()
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
        instance = Bukkit.getPluginManager().getPlugin("StackMob") as StackMob
    }
}
