package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
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

        event.minion.setActions(event.minion.getActionAmount() + entitySize)
        event.minion.setStorage(event.minion.getStorage() + ThreadLocalRandom.current().nextInt(1, 4) * entitySize)

        event.target.location.world!!.getNearbyEntities(event.target.location, 4.0, 4.0, 4.0).filterIsInstance<Item>().fastFor { item ->
            if (event.minion.getLinkedInventory()?.firstEmpty() == -1) {
                Warnings.CONTAINER_FULL.display(event.minion)
                return
            }

            val amount = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(item)
            val stack = item.itemStack
            stack.amount = amount.toInt()

            event.minion.addToContainerOrDrop(stack)
            item.remove()
        }
    }
}