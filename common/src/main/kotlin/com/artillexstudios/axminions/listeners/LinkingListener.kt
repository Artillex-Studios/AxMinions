package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.events.MinionChestLinkEvent
import com.artillexstudios.axminions.api.events.PreMinionDamageEntityEvent
import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.Bukkit
import java.util.WeakHashMap
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.min

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

        val linkEvent = MinionChestLinkEvent(
            minion,
            event.player,
            event.clickedBlock!!
        )
        Bukkit.getPluginManager().callEvent(linkEvent)
        if (linkEvent.isCancelled) {
            event.player.sendMessage(StringUtils.formatToString(linkEvent.getFailMessage() ?: (Messages.PREFIX() + Messages.LINK_FAIL())))
            return
        }

        event.isCancelled = true
        if (minion.getLocation()
                .distanceSquared(event.clickedBlock!!.location) > Config.MAX_LINKING_DISTANCE() * Config.MAX_LINKING_DISTANCE()
        ) {
            event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_FAIL()))
            return
        }

        val min = minion as com.artillexstudios.axminions.minions.Minion
        if (min.broken.get()) {
            return
        }

        event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_SUCCESS()))
        minion.setLinkedChest(event.clickedBlock!!.location)
    }
}