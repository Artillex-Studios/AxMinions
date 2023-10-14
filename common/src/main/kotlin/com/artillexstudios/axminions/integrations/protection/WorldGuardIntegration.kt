package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import org.bukkit.Location
import org.bukkit.entity.Player

class WorldGuardIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val world = BukkitAdapter.adapt(player.world)

        if (WorldGuard.getInstance().platform.sessionManager.hasBypass(localPlayer, world)) {
            return true
        }

        val container = WorldGuard.getInstance().platform.regionContainer
        val query = container.createQuery()

        return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BUILD)
    }

    override fun register() {

    }
}