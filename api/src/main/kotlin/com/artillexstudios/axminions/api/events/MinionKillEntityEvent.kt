package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.entity.LivingEntity
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class MinionKillEntityEvent(minion: Minion, val target: LivingEntity) : MinionEvent(minion) {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}