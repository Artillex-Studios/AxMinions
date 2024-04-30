package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class MinionPreBreakEvent(private val player: Player, minion: Minion) : MinionEvent(minion), Cancellable {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    private var isCancelled = false

    override fun getHandlers(): HandlerList {
        return MinionPreBreakEvent.handlerList
    }

    fun getPlayer(): Player {
        return player
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        isCancelled = cancelled
    }
}