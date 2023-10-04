package com.artillexstudios.axminions

import com.artillexstudios.axapi.AxPlugin
import com.artillexstudios.axapi.data.ThreadedQueue
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.AxMinionsAPIImpl
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.commands.AxMinionsCommand
import com.artillexstudios.axminions.data.H2DataHandler
import com.artillexstudios.axminions.minions.miniontype.CollectorMinionType
import com.artillexstudios.axminions.minions.miniontype.FarmerMinionType
import net.byteflux.libby.BukkitLibraryManager
import net.byteflux.libby.Library
import revxrsal.commands.bukkit.BukkitCommandHandler
import java.io.File

class AxMinionsPlugin : AxPlugin() {
    companion object {
        lateinit var INSTANCE: AxMinionsPlugin
        lateinit var messages: Messages
        lateinit var config: Config
        lateinit var dataHandler: DataHandler
        lateinit var dataQueue: ThreadedQueue<Runnable>
    }

    init {
        val manager = BukkitLibraryManager(this)
        val stdLib = Library.builder().groupId("org.jetbrains.kotlin").artifactId("kotlin-stdlib").relocate("org.jetbrains.kotlin", "com.artillexstudios.axminions.libs.kotlin").version("1.9.0").build()
        val h2 = Library.builder().groupId("com.h2database").artifactId("h2").version("2.2.220").relocate("org.h2", "com.artillexstudios.axminions.libs.h2").build()
        manager.addMavenCentral()
        manager.loadLibrary(stdLib)
        manager.loadLibrary(h2)
    }

    override fun load() {
        INSTANCE = this
        AxMinionsAPI.INSTANCE = AxMinionsAPIImpl(this)
    }

    override fun enable() {
        AxMinionsPlugin.config = Config(File(dataFolder, "config.yml"), getResource("config.yml")!!)
        messages = Messages(File(dataFolder, "messages.yml"), getResource("messages.yml")!!)

        loadDataHandler()
        dataQueue = ThreadedQueue("AxMinions-Database-Queue")

        MinionTypes.also {
            it.register(CollectorMinionType())
            it.register(FarmerMinionType())
        }

        val handler = BukkitCommandHandler.create(this)

        handler.registerValueResolver(MinionType::class.java) { c ->
            val type = c.popForParameter()

            val minionType = MinionTypes.valueOf(type) ?: return@registerValueResolver MinionTypes.valueOf("collector")


            return@registerValueResolver minionType
        }

        handler.autoCompleter.registerSuggestion("minionTypes") { _, _, _ ->
            return@registerSuggestion MinionTypes.getMinionTypes().values.map { it.getName() }
        }

        handler.register(AxMinionsCommand())

        handler.registerBrigadier()
    }

    private fun loadDataHandler() {
        if (Config.DATABASE_TYPE().equals("H2", true)) {
            dataHandler = H2DataHandler()
            dataHandler.setup()
        } else {

        }
    }
}