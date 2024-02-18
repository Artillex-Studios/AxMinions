package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.minions.Minions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        Minions.getMinions().forEach {
            if (it.getOwnerUUID() == event.player.uniqueId) {
                it.setOwnerOnline(true)
            }
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerQuitEvent) {
        Minions.getMinions().forEach {
            if (it.getOwnerUUID() == event.player.uniqueId) {
                it.setOwnerOnline(false)
            }
        }
    }
}