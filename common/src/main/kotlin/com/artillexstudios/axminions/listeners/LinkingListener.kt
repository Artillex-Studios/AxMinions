package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Minion
import java.util.WeakHashMap
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class LinkingListener : Listener {
    companion object {
        val linking = WeakHashMap<Player, Minion>()
        private val CONTAINERS = listOf(Material.BARREL, Material.CHEST, Material.TRAPPED_CHEST)
    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.clickedBlock == null) return
        if (event.player !in linking) return
        if (event.clickedBlock!!.type !in CONTAINERS) return
        if (!AxMinionsPlugin.integrations.getProtectionIntegration().canBuildAt(event.player, event.clickedBlock!!.location)) return

        val minion = linking.remove(event.player) ?: return
        event.isCancelled = true
        if (minion.getLocation()
                .distanceSquared(event.clickedBlock!!.location) > Config.MAX_LINKING_DISTANCE() * Config.MAX_LINKING_DISTANCE()
        ) {
            event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_FAIL()))
            return
        }

        event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_SUCCESS()))
        minion.setLinkedChest(event.clickedBlock!!.location)
    }
}