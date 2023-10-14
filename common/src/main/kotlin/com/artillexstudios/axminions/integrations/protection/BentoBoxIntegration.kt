package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import org.bukkit.Location
import org.bukkit.entity.Player
import world.bentobox.bentobox.BentoBox

class BentoBoxIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        val island = BentoBox.getInstance().islands.getIslandAt(location)

        return island.map {
            player.uniqueId in it.memberSet
        }.orElse(true)
    }

    override fun register() {

    }
}