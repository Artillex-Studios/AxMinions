package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import org.bukkit.Location
import org.bukkit.entity.Player
import org.kingdoms.constants.land.Land
import org.kingdoms.constants.player.KingdomPlayer

class KingdomsXIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        val localPlayer = KingdomPlayer.getKingdomPlayer(player.uniqueId);
        val land = Land.getLand(location) ?: return true

        return land.kingdom.isMember(localPlayer);
    }

    override fun register() {

    }
}