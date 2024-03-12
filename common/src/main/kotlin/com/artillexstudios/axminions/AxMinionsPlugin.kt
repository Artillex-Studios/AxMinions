package com.artillexstudios.axminions

import com.artillexstudios.axapi.AxPlugin
import com.artillexstudios.axapi.data.ThreadedQueue
import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.utils.FeatureFlags
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.AxMinionsAPIImpl
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.commands.AxMinionsCommand
import com.artillexstudios.axminions.data.H2DataHandler
import com.artillexstudios.axminions.integrations.Integrations
import com.artillexstudios.axminions.listeners.ChunkListener
import com.artillexstudios.axminions.listeners.LinkingListener
import com.artillexstudios.axminions.listeners.MinionDamageListener
import com.artillexstudios.axminions.listeners.MinionDropListener
import com.artillexstudios.axminions.listeners.MinionInventoryListener
import com.artillexstudios.axminions.listeners.MinionPlaceListener
import com.artillexstudios.axminions.listeners.PlayerListener
import com.artillexstudios.axminions.listeners.WorldListener
import com.artillexstudios.axminions.minions.Minion
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.minions.Minions
import com.artillexstudios.axminions.minions.miniontype.CollectorMinionType
import com.artillexstudios.axminions.minions.miniontype.FarmerMinionType
import com.artillexstudios.axminions.minions.miniontype.FisherMinionType
import com.artillexstudios.axminions.minions.miniontype.LumberMinionType
import com.artillexstudios.axminions.minions.miniontype.MinerMinionType
import com.artillexstudios.axminions.minions.miniontype.SellerMinionType
import com.artillexstudios.axminions.minions.miniontype.SlayerMinionType
import java.io.File
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager
import com.artillexstudios.axapi.libs.libby.Library
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import revxrsal.commands.bukkit.BukkitCommandHandler

class AxMinionsPlugin : AxPlugin() {
    companion object {
        lateinit var INSTANCE: AxMinionsPlugin
        lateinit var messages: Messages
        lateinit var config: Config
        lateinit var dataHandler: DataHandler
        lateinit var dataQueue: ThreadedQueue<Runnable>
        lateinit var integrations: Integrations
    }

    init {
        val manager = BukkitLibraryManager(this)
        val stdLib = Library.builder().groupId("org{}jetbrains{}kotlin").artifactId("kotlin-stdlib").version("1.9.22")
            .relocate("org{}jetbrains{}kotlin", "com{}artillexstudios{}axminions{}libs{}kotlin").build()
        val h2 = Library.builder().groupId("com{}h2database").artifactId("h2").version("2.2.220")
            .relocate("org{}h2", "com{}artillexstudios{}axminions{}libs{}h2").build()
        manager.addMavenCentral()
        manager.loadLibrary(stdLib)
        manager.loadLibrary(h2)
    }

    override fun updateFlags() {
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true)
    }

    override fun load() {
        INSTANCE = this
        AxMinionsAPI.INSTANCE = AxMinionsAPIImpl(this)
    }

    override fun enable() {
        Metrics(this, 19043)

        AxMinionsPlugin.config = Config(File(dataFolder, "config.yml"), getResource("config.yml")!!)
        messages = Messages(File(dataFolder, "messages.yml"), getResource("messages.yml")!!)
        integrations = Integrations()
        integrations.reload()

        loadDataHandler()
        dataQueue = ThreadedQueue("AxMinions-Database-Queue")

        MinionTypes.also {
            it.register(CollectorMinionType())
            it.register(FarmerMinionType())
            it.register(MinerMinionType())
            it.register(LumberMinionType())
            it.register(SellerMinionType())
            it.register(FisherMinionType())
            it.register(SlayerMinionType())
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

        if (handler.isBrigadierSupported) {
            handler.registerBrigadier()
        }

        Bukkit.getPluginManager().also {
            it.registerEvents(MinionPlaceListener(), this)
            it.registerEvents(LinkingListener(), this)
            it.registerEvents(MinionInventoryListener(), this)
            it.registerEvents(ChunkListener(), this)
            it.registerEvents(MinionDamageListener(), this)
            it.registerEvents(WorldListener(), this)
            it.registerEvents(MinionDropListener(), this)
            it.registerEvents(PlayerListener(), this)
        }

        // Retroactively load minions for the already loaded worlds
        Bukkit.getWorlds().fastFor { world ->
            MinionTypes.getMinionTypes().fastFor { _, v ->
                dataHandler.loadMinionsForWorld(v, world)
            }

            world.loadedChunks.fastFor {
                Minions.startTicking(it)
            }
        }

        MinionTicker.startTicking()

        Scheduler.get().runTimer({
            dataQueue.submit {
                Minions.get {
                    it.fastFor { pos ->
                        pos.minions.fastFor { minion ->
                            dataHandler.saveMinion(minion)
                        }
                    }
                }
            }
        }, 0, Config.AUTO_SAVE_MINUTES() * 20 * 60)
    }

    override fun disable() {
        Minions.get {
            it.fastFor { pos ->
                pos.minions.fastFor { minion ->
                    val minionImp = minion as Minion

                    minionImp.openInventories.fastFor { inventory ->
                        inventory.viewers.fastFor { player ->
                            player.closeInventory()
                        }
                    }
                }
            }
        }

        dataHandler.disable()
    }

    private fun loadDataHandler() {
        if (Config.DATABASE_TYPE().equals("H2", true)) {
            dataHandler = H2DataHandler()
            dataHandler.setup()
        }
    }
}