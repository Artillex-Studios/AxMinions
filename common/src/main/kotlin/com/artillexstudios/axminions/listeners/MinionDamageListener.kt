package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.events.MinionKillEntityEvent
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.nms.NMSHandler
import org.bukkit.Bukkit
import java.util.concurrent.ThreadLocalRandom
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class MinionDamageListener : Listener {

    @EventHandler
    fun onMinionKillEntityEvent(event: MinionKillEntityEvent) {
        val entitySize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(event.target)

        event.minion.setActions(event.minion.getActionAmount() + entitySize)
        val coerced = (event.minion.getStorage() + ThreadLocalRandom.current().nextInt(1, 4) * entitySize).coerceIn(
            0.0,
            event.minion.getType().getLong("storage", event.minion.getLevel()).toDouble()
        )
        event.minion.setStorage(coerced)

        Scheduler.get().runLaterAt(event.target.location, { task ->
            event.target.location.world!!.getNearbyEntities(event.target.location, 4.0, 4.0, 4.0)
                .filterIsInstance<Item>().fastFor { item ->
                if (event.minion.getLinkedInventory()?.firstEmpty() == -1) {
                    Warnings.CONTAINER_FULL.display(event.minion)
                    return@runLaterAt
                }

                val amount = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(item)
                val stack = item.itemStack
                stack.amount = amount.toInt()

                val map = event.minion.addWithRemaining(stack) ?: return@fastFor
                if (map.isEmpty() || stack.amount <= 0) {
                    item.remove()
                } else {
                    AxMinionsAPI.INSTANCE.getIntegrations().getStackerIntegration().setStackSize(item, stack.amount)
                }
            }
        }, 2)
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.damager.uniqueId == NMSHandler.get().getAnimalUUID() && event.entity is Player) {
            event.isCancelled = true
            event.damage = 0.0
            return
        }

        val entity = event.entity
        if (entity is LivingEntity) {
            if (event.damager.uniqueId == NMSHandler.get().getAnimalUUID() && event.finalDamage > entity.health) {
                Bukkit.getPluginManager().callEvent(MinionKillEntityEvent(NMSHandler.get().getMinion() ?: return, entity))
            }
        }
    }
}