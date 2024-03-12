package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.events.MinionKillEntityEvent
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import java.util.concurrent.ThreadLocalRandom
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MinionDamageListener : Listener {

    @EventHandler
    fun onMinionKillEntityEvent(event: MinionKillEntityEvent) {
        val entitySize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(event.target)

        val amount = ThreadLocalRandom.current().nextInt(1, 4) * entitySize
        event.minion.setActions(event.minion.getActionAmount() + entitySize)
        if (event.minion.getStorage() + amount < event.minion.getType().getDouble("storage", event.minion.getLevel())) {
            event.minion.setStorage(event.minion.getStorage() + amount * entitySize)
        }

        Scheduler.get().runLaterAt(event.target.location, {
            event.target.location.world!!.getNearbyEntities(event.target.location, 4.0, 4.0, 4.0).filterIsInstance<Item>().fastFor { item ->
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
}