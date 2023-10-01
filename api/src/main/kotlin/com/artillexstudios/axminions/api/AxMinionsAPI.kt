package com.artillexstudios.axminions.api

import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import java.io.File

interface AxMinionsAPI {

    fun getAxMinionsDataFolder(): File

    fun getMessages(): Messages

    fun getConfig(): Config

    fun getDataHandler(): DataHandler

    companion object {
        @JvmStatic
        lateinit var INSTANCE: AxMinionsAPI
    }
}