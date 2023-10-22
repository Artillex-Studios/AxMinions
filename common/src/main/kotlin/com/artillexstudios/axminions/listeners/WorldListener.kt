package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class WorldListener : Listener {

    @EventHandler
    fun onWorldLoadEvent(event: WorldLoadEvent) {
        AxMinionsPlugin.dataQueue.submit {
            MinionTypes.loadForWorld(event.world)
        }
    }
}