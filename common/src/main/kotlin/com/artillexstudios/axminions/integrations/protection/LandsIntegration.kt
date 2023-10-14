package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import me.angeschossen.lands.api.LandsIntegration
import me.angeschossen.lands.api.flags.type.Flags
import org.bukkit.Location
import org.bukkit.entity.Player

class LandsIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        val api = LandsIntegration.of(AxMinionsPlugin.INSTANCE)
        val world = api.getWorld(location.world ?: return true) ?: return true

        return world.hasRoleFlag(player.uniqueId, location, Flags.BLOCK_PLACE);
    }

    override fun register() {

    }
}