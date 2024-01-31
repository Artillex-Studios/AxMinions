package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class WorldListener : Listener {

    @EventHandler
    fun onWorldLoadEvent(event: WorldLoadEvent) {
        MinionTypes.loadForWorld(event.world)
    }
}