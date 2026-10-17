package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.minions.Minions
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class LinkedChestListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val block = event.block
        // Cheap type check first, so we only look through the minions when a container is broken
        if (block.type !in LinkingListener.CONTAINERS) return
        if (!isLinked(block)) return

        event.isCancelled = true
        event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_CHEST_PROTECTED()))
    }

    private fun isLinked(block: Block): Boolean {
        return Minions.getMinions().any {
            val chest = it.getLinkedChest() ?: return@any false

            chest.world == block.world && chest.blockX == block.x && chest.blockY == block.y && chest.blockZ == block.z
        }
    }
}
