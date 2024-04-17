package com.artillexstudios.axminions.integrations.placeholder

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderAPIIntegration : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "axminions"
    }

    override fun getAuthor(): String {
        return "Artillex-Studios"
    }

    override fun getVersion(): String {
        return AxMinionsPlugin.INSTANCE.description.version
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        return when (params) {
            "placed" -> {
                return player?.uniqueId?.let { AxMinionsPlugin.dataHandler.getMinionAmount(it).toString() } ?: ""
            }
            "limit" -> {
                return player?.let { AxMinionsAPI.INSTANCE.getMinionLimit(it).toString() } ?: ""
            }
            else -> ""
        }
    }
}