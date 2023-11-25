package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.api.utils.Keys
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.persistence.PersistentDataType

class MinionDropListener : Listener {

    @EventHandler
    fun onPlayerDropItemEvent(event: PlayerDropItemEvent) {
        val item = event.itemDrop.itemStack
        if (item.type.isAir) return
        val meta = item.itemMeta ?: return

        if (!meta.persistentDataContainer.has(Keys.PLACED, PersistentDataType.BYTE)) return

        event.isCancelled = true
    }
}