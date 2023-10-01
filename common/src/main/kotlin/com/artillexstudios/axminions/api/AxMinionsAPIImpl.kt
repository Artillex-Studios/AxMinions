package com.artillexstudios.axminions.api

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import java.io.File

class AxMinionsAPIImpl(private val plugin: AxMinionsPlugin) : AxMinionsAPI {
    private val messages = Messages(File(getAxMinionsDataFolder(), "messages.yml"), plugin.getResource("messages.yml")!!)
    private val config = Config(File(getAxMinionsDataFolder(), "config.yml"), plugin.getResource("config.yml")!!)

    override fun getAxMinionsDataFolder(): File {
        return plugin.dataFolder
    }

    override fun getMessages(): Messages {
        return messages
    }

    override fun getConfig(): Config {
        return config
    }

    override fun getDataHandler(): DataHandler {
        TODO("Not yet implemented")
    }
}