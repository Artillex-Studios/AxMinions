package com.artillexstudios.axminions.api.events

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class PreFarmerMinionHarvestEvent(minion: Minion, val block: Block) : MinionEvent(minion), Cancellable {
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

    fun getHarvestBlock(): Block {
        return block
    }

    fun setType(): Block {
        return block
    }

    override fun setCancelled(cancelled: Boolean) {
        isCancelled = cancelled
    }
}