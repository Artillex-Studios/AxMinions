package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.utils.CoolDown
import com.artillexstudios.axminions.users.MinionUsers
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent

class MinionUsersListener : Listener {
    private val coolDown = CoolDown<Player>()

    @EventHandler
    fun onInventoryDragEvent(event: InventoryDragEvent) {
        if (event.inventory.holder !is MinionUsers.Holder) return

        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? MinionUsers.Holder ?: return
        event.isCancelled = true

        if (event.clickedInventory != event.inventory) return

        val player = event.whoClicked as? Player ?: return

        if (coolDown.contains(player)) {
            return
        }

        coolDown.add(player, 250)

        AxMinionsPlugin.users.click(player, holder, event.slot)
    }

    // The name of a new trusted player is typed in the chat, it has to be handled before any chat plugin sees it
    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        if (AxMinionsPlugin.users.handleChat(event.player, event.message)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        AxMinionsPlugin.users.cancelPrompt(event.player.uniqueId)
    }
}
