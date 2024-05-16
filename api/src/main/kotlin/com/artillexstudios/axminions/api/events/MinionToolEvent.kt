package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class MinionToolEvent(minion: Minion, private val player: Player, private val newTool: ItemStack) : MinionEvent(minion), Cancellable {
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

    fun getPlayer(): Player {
        return player
    }

    fun getNewTool(): ItemStack {
        return newTool
    }

    override fun setCancelled(cancelled: Boolean) {
        isCancelled = cancelled
    }
}