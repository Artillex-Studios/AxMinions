package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.minions.Minions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

class WorldListener : Listener {

    @EventHandler
    fun onWorldLoadEvent(event: WorldLoadEvent) {
        MinionTypes.loadForWorld(event.world)

        event.world.loadedChunks.fastFor {
            Minions.startTicking(it)
        }
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent) {
        val worldUUID = event.world.uid
        Minions.getMinions().fastFor {
            if (it.getLocation().world?.uid == worldUUID) {
                Minions.remove(it)
            }
        }
    }
}