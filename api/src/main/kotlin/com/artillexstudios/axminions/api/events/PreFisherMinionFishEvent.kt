package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class PreFisherMinionFishEvent(minion: Minion, var item: List<ItemStack>) : MinionEvent(minion), Cancellable {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    private var isCancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    fun getLoots(): List<ItemStack> {
        return item
    }

    fun setLoots(item: List<ItemStack>) {
        this.item = item
    }

    fun setLoot(item: ItemStack) {
        this.item = listOf(item)
    }

    override fun setCancelled(cancelled: Boolean) {
        isCancelled = cancelled
    }
}