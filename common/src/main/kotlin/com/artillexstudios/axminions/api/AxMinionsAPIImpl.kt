package com.artillexstudios.axminions.api

import com.artillexstudios.axapi.AxPlugin
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.integrations.Integrations
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.minions.Minions
import org.bukkit.entity.Player
import java.io.File
import java.util.Locale

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

        player.effectivePermissions.forEach {
            val permission = it.permission
            if (permission.equals("*", true)) {
                return Int.MAX_VALUE
            }

            if (!permission.startsWith("axminions.limit.")) return@forEach
            if (permission.contains("*")) {
                return Int.MAX_VALUE
            }
            val subString = permission.substring(permission.lastIndexOf('.') + 1)
            if (subString.isBlank()) return@forEach

            val value = subString.toInt()

            if (value > limit) {
                limit = value
            }
        }
        limit += AxMinionsPlugin.dataHandler.getExtraSlots(player.uniqueId)
        return limit
    }

    override fun getIntegrations(): Integrations {
        return AxMinionsPlugin.integrations
    }

    override fun getTick(): Long {
        return MinionTicker.getTick()
    }
}