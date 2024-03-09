package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import com.palmergames.bukkit.towny.`object`.TownyPermission
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

class TownyIntegration : ProtectionIntegration {

    override fun canBuildAt(player: Player, location: Location): Boolean {
        return PlayerCacheUtil.getCachePermission(player, location, Material.STONE, TownyPermission.ActionType.BUILD);
    }

    override fun register() {

    }
}