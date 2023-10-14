package com.artillexstudios.axminions.integrations.protection

import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegrations
import com.artillexstudios.axminions.api.utils.fastFor
import java.util.Collections
import org.bukkit.Location
import org.bukkit.entity.Player

class ProtectionIntegrations : ProtectionIntegrations {
    private val integrations = arrayListOf<ProtectionIntegration>()

    override fun getProtectionIntegrations(): List<ProtectionIntegration> {
        return Collections.unmodifiableList(integrations)
    }

    override fun canBuildAt(player: Player, location: Location): Boolean {
        integrations.fastFor {
            if (!it.canBuildAt(player, location)) {
                return false
            }
        }

        return true
    }

    fun clear() {
        integrations.clear()
    }

    fun register(integration: ProtectionIntegration) {
        integrations.add(integration)
    }

    fun deregister(integration: ProtectionIntegration) {
        integrations.remove(integration)
    }
}