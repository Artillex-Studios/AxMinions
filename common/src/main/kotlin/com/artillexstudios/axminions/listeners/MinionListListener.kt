package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.utils.CoolDown
import com.artillexstudios.axminions.list.MinionList
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class MinionListListener : Listener {
    private val coolDown = CoolDown<Player>()

    @EventHandler
    fun onInventoryDragEvent(event: InventoryDragEvent) {
        if (event.inventory.holder !is MinionList.Holder) return

        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? MinionList.Holder ?: return
        event.isCancelled = true

        if (event.clickedInventory != event.inventory) return

        val player = event.whoClicked as? Player ?: return

        if (coolDown.contains(player)) {
            return
        }

        coolDown.add(player, 250)

        AxMinionsPlugin.minionList.click(player, holder, event.slot, event.click)
    }
}
