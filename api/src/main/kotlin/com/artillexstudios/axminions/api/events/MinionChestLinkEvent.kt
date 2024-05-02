package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class MinionChestLinkEvent(minion: Minion, private val player: Player, private val block: Block) : MinionEvent(minion), Cancellable {
    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    private var isCancelled = false
    private var failMessage: String? = null

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    fun getLinker(): Player {
        return player
    }

    fun getFailMessage(): String? {
        return failMessage
    }

    fun setFailMessage(message: String) {
        failMessage = message
    }

    fun getBlock(): Block {
        return block
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        isCancelled = cancelled
    }
}