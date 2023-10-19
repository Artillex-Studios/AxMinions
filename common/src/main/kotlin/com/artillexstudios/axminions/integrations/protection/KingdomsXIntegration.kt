package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import org.bukkit.Location
import org.bukkit.entity.Player

class KingdomsXIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
//        val localPlayer = KingdomPlayer.getKingdomPlayer(player.uniqueId);
//        val land = Land.getLand(location) ?: return true
//
//        return land.kingdom.isMember(localPlayer);
        return true
    }

    override fun register() {

    }
}