package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.minions.Minion
import com.artillexstudios.axminions.minions.Minions
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MinionPlaceListener : Listener {

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        if (event.clickedBlock == null) return
        if (event.item == null) return
        if (!event.item!!.hasItemMeta()) return

        val type = event.item!!.itemMeta!!.persistentDataContainer.get(Keys.MINION_TYPE, PersistentDataType.STRING) ?: return
        val minionType = MinionTypes.valueOf(type) ?: return
        event.isCancelled = true

        val item = event.item ?: return
        var meta = item.itemMeta ?: return
        if (!AxMinionsPlugin.integrations.getProtectionIntegration().canBuildAt(event.player, event.clickedBlock!!.location)) return
        val level = meta.persistentDataContainer.get(Keys.LEVEL, PersistentDataType.INTEGER) ?: 0
        val stats = meta.persistentDataContainer.get(Keys.STATISTICS, PersistentDataType.LONG) ?: 0
        if (Config.PLACE_PERMISSION() && !event.player.hasPermission("axminions.place.${minionType.getName()}")) {
            event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.PLACE_MISSING_PERMISSION()))
            return
        }

        if (meta.persistentDataContainer.has(Keys.PLACED, PersistentDataType.BYTE)) return
        meta.persistentDataContainer.set(Keys.PLACED, PersistentDataType.BYTE, 0)
        item.itemMeta = meta

        val location = event.clickedBlock!!.getRelative(event.blockFace).location

        val maxMinions = AxMinionsAPI.INSTANCE.getMinionLimit(event.player)

        val chunk = location.chunk
        AxMinionsPlugin.dataQueue.submit {
            val placed = AxMinionsPlugin.dataHandler.getMinionAmount(event.player.uniqueId)

            if (placed >= maxMinions && !event.player.hasPermission("axminions.limit.*")) {
                event.player.sendMessage(
                    StringUtils.formatToString(
                        Messages.PREFIX() + Messages.PLACE_LIMIT_REACHED(),
                        Placeholder.unparsed("placed", placed.toString()),
                        Placeholder.unparsed("max", maxMinions.toString())
                    )
                )
                return@submit
            }

            if (AxMinionsPlugin.dataHandler.isMinion(location)) {
                event.player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.PLACE_MINION_AT_LOCATION()))
                return@submit
            }

            val locationId = AxMinionsPlugin.dataHandler.getLocationID(location)
            val minion = Minion(
                location,
                event.player.uniqueId,
                event.player,
                minionType,
                level,
                ItemStack(Material.AIR),
                null,
                Direction.NORTH,
                stats,
                0.0,
                locationId,
                0
            )
            Minions.startTicking(chunk)

            Scheduler.get().run { _ ->
                meta = item.itemMeta!!
                meta.persistentDataContainer.remove(Keys.PLACED)
                item.itemMeta = meta
                item.amount = item.amount.minus(1)
            }

            if (Config.DEBUG()) {
                event.player.sendMessage(
                    "Placed minion $minion. Ticking? ${minion.isTicking()} Is chunk ticking? ${
                        Minions.isTicking(
                            chunk
                        )
                    }"
                )
            }

            AxMinionsPlugin.dataHandler.saveMinion(minion)

            event.player.sendMessage(
                StringUtils.formatToString(
                    Messages.PREFIX() + Messages.PLACE_SUCCESS(),
                    Placeholder.unparsed("type", minionType.getName()),
                    Placeholder.unparsed("placed", (placed + 1).toString()),
                    Placeholder.unparsed("max", (maxMinions).toString())
                )
            )
        }
    }
}