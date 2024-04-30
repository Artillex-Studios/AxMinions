package com.artillexstudios.axminions.api.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MinionPrePlaceEvent(private val player: Player) : Cancellable, Event() {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    private var isCancelled = false


    override fun getHandlers(): HandlerList {
        return MinionPrePlaceEvent.handlerList
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    fun getPlacer(): Player {
        return player
    }

    override fun setCancelled(cancelled: Boolean) {
        isCancelled = cancelled
    }
}