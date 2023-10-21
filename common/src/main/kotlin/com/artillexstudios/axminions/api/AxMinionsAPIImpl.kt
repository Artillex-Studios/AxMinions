package com.artillexstudios.axminions.api

import com.artillexstudios.axapi.AxPlugin
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.integrations.Integrations
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.minions.Minions
import org.bukkit.entity.Player
import java.io.File

class AxMinionsAPIImpl(private val plugin: AxMinionsPlugin) : AxMinionsAPI {

    override fun getAxMinionsDataFolder(): File {
        return plugin.dataFolder
    }

    override fun getMessages(): Messages {
        return AxMinionsPlugin.messages
    }

    override fun getConfig(): Config {
        return AxMinionsPlugin.config
    }

    override fun getDataHandler(): DataHandler {
        return AxMinionsPlugin.dataHandler
    }

    override fun getMinions(): List<Minion> {
        return Minions.getMinions()
    }

    override fun getAxMinionsInstance(): AxPlugin {
        return plugin
    }

    override fun getMinionLimit(player: Player): Int {
        var limit = Config.DEFAULT_MINION_LIMIT()

        player.effectivePermissions.fastFor {
            val permission = it.permission
            if (!permission.startsWith("axminions.limit.")) return@fastFor
            if (permission.contains("*")) {
                return Int.MAX_VALUE
            }

            val value = permission.substring(permission.lastIndexOf('.') + 1).toInt()

            if (value > limit) {
                limit = value
            }
        }

        return limit
    }

    override fun getIntegrations(): Integrations {
        return AxMinionsPlugin.integrations
    }
}