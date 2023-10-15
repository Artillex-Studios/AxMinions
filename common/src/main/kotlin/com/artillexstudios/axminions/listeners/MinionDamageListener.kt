package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.events.MinionDamageEntityEvent
import java.util.concurrent.ThreadLocalRandom
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MinionDamageListener : Listener {

    @EventHandler
    fun onMinionDamageEntityEvent(event: MinionDamageEntityEvent) {
        if (event.damage < event.target.health) return

        val entitySize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(event.target)

        event.minion.setStorage(event.minion.getStorage() + ThreadLocalRandom.current().nextInt(1, 4) * entitySize)
    }
}