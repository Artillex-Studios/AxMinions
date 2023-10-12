package com.artillexstudios.axminions.api

import com.artillexstudios.axapi.AxPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.integrations.Integrations
import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.entity.Player
import java.io.File

interface AxMinionsAPI {

    fun getAxMinionsDataFolder(): File

    fun getMessages(): Messages

    fun getConfig(): Config

    fun getDataHandler(): DataHandler

    fun getMinions(): List<Minion>

    fun getAxMinionsInstance(): AxPlugin

    fun getMinionLimit(player: Player): Int

    fun getIntegrations(): Integrations

    companion object {
        @JvmStatic
        lateinit var INSTANCE: AxMinionsAPI
    }
}