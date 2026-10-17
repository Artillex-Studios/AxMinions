package com.artillexstudios.axminions.data

import com.artillexstudios.axapi.serializers.Serializers
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level

/**
 * Keeps all data in memory and writes it to data.yml shortly after it changes,
 * so saving every minion during an auto-save results in a single file write.
 */
class YamlDataHandler : DataHandler {
    private companion object {
        const val SAVE_DELAY_SECONDS = 5L
    }

    private data class StoredLocation(val world: String, val x: Int, val y: Int, val z: Int)

    private class StoredMinion(
        val type: String,
        val owner: UUID,
        val chestLocationId: Int,
        val direction: Direction,
        val level: Int,
        val storage: Double,
        val actions: Long,
        val tool: String?,
        val charge: Long
    )

    private class StoredUser(var name: String, var islandSlots: Int)

    private val file = File(AxMinionsPlugin.INSTANCE.dataFolder, "data.yml")
    private val lock = Any()
    private val fileLock = Any()
    private val locations = HashMap<Int, StoredLocation>()
    private val locationIds = HashMap<StoredLocation, Int>()
    private val minions = HashMap<Int, StoredMinion>()
    private val users = HashMap<UUID, StoredUser>()
    private val islands = HashMap<String, Int>()
    // World ids are not persisted, they only exist to back getWorld(worldId)
    private val worlds = ArrayList<String>()
    private var nextLocationId = 1
    private var loaded = false

    private val saveExecutor = ScheduledThreadPoolExecutor(1) { Thread(it, "AxMinions-Yaml-Saver").apply { isDaemon = true } }.apply {
        // Pending saves are dropped on shutdown, disable() writes the file itself
        executeExistingDelayedTasksAfterShutdownPolicy = false
    }
    private val saveScheduled = AtomicBoolean(false)

    override fun getType(): String {
        return "YAML"
    }

    override fun setup() {
        if (file.exists()) {
            val yaml = YamlConfiguration()
            try {
                yaml.load(file)
            } catch (exception: Exception) {
                // Refuse to start instead of overwriting the broken file with empty data
                throw IllegalStateException("Could not load ${file.path}! Fix or remove the file and restart the server.", exception)
            }

            synchronized(lock) {
                yaml.getConfigurationSection("locations")?.let { section ->
                    for (key in section.getKeys(false)) {
                        val id = key.toIntOrNull() ?: continue
                        val world = section.getString("$key.world") ?: continue
                        registerLocation(id, StoredLocation(world, section.getInt("$key.x"), section.getInt("$key.y"), section.getInt("$key.z")))
                    }
                }

                yaml.getConfigurationSection("users")?.let { section ->
                    for (key in section.getKeys(false)) {
                        val uuid = runCatching { UUID.fromString(key) }.getOrNull() ?: continue
                        users[uuid] = StoredUser(section.getString("$key.name") ?: "---", section.getInt("$key.island-slots"))
                    }
                }

                yaml.getConfigurationSection("minions")?.let { section ->
                    for (key in section.getKeys(false)) {
                        val id = key.toIntOrNull() ?: continue
                        val type = section.getString("$key.type") ?: continue
                        val owner = runCatching { UUID.fromString(section.getString("$key.owner")) }.getOrNull() ?: continue
                        val direction = runCatching { Direction.valueOf(section.getString("$key.direction")!!) }.getOrDefault(Direction.NORTH)

                        minions[id] = StoredMinion(
                            type,
                            owner,
                            section.getInt("$key.chest-location"),
                            direction,
                            section.getInt("$key.level", 1),
                            section.getDouble("$key.storage"),
                            section.getLong("$key.actions"),
                            section.getString("$key.tool"),
                            section.getLong("$key.charge")
                        )
                    }
                }

                for (map in yaml.getMapList("islands")) {
                    val island = map["island"]?.toString() ?: continue
                    islands[island] = (map["placed"] as? Number)?.toInt() ?: 0
                }

                nextLocationId = maxOf(nextLocationId, yaml.getInt("next-location-id", 1))
            }
        }

        loaded = true
    }

    override fun insertType(minionType: MinionType) {
        // Minion types are stored by their name, there is nothing to register
    }

    override fun loadMinionsForWorld(minionType: MinionType, world: World) {
        // Minions are created outside the lock, so their constructors can never block other data calls
        val loading = ArrayList<() -> Unit>()

        synchronized(lock) {
            for ((locationId, stored) in minions) {
                if (stored.type != minionType.getName()) {
                    continue
                }

                val storedLocation = locations[locationId] ?: continue
                if (storedLocation.world != world.name) {
                    continue
                }

                val location = Location(world, storedLocation.x.toDouble(), storedLocation.y.toDouble(), storedLocation.z.toDouble())
                val chestLocation = locations[stored.chestLocationId]?.let {
                    Location(Bukkit.getWorld(it.world), it.x.toDouble(), it.y.toDouble(), it.z.toDouble())
                }

                loading.add {
                    var itemStack = ItemStack(Material.AIR)
                    if (stored.tool != null) {
                        itemStack = Serializers.ITEM_STACK.deserialize(stored.tool)
                    }

                    com.artillexstudios.axminions.minions.Minion(
                        location,
                        stored.owner,
                        Bukkit.getOfflinePlayer(stored.owner),
                        minionType,
                        stored.level,
                        itemStack,
                        chestLocation,
                        stored.direction,
                        stored.actions,
                        stored.storage,
                        locationId,
                        if (chestLocation == null) 0 else stored.chestLocationId,
                        stored.charge
                    )
                }
            }
        }

        loading.forEach { it() }
    }

    override fun getLocationID(location: Location): Int {
        val key = StoredLocation(location.world?.name ?: "---", location.blockX, location.blockY, location.blockZ)
        synchronized(lock) {
            locationIds[key]?.let { return it }

            val id = nextLocationId
            registerLocation(id, key)
            markDirty()
            return id
        }
    }

    override fun getLocation(locationId: Int): Location? {
        val stored = synchronized(lock) { locations[locationId] } ?: return null
        return Location(Bukkit.getWorld(stored.world), stored.x.toDouble(), stored.y.toDouble(), stored.z.toDouble())
    }

    override fun getWorld(worldId: Int): World? {
        val name = synchronized(lock) { worlds.getOrNull(worldId - 1) } ?: return null
        return Bukkit.getWorld(name)
    }

    override fun saveMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        val locationId = getLocationID(minion.getLocation())
        val linkedChestId = minion.getLinkedChest()?.let { getLocationID(it) } ?: 0
        val tool = minion.getTool()?.takeIf { it.type != Material.AIR }?.let { Serializers.ITEM_STACK.serialize(it) }

        synchronized(lock) {
            val name = minion.getOwner()?.name ?: "---"
            users.getOrPut(minion.getOwnerUUID()) { StoredUser(name, 0) }.name = name

            minions[locationId] = StoredMinion(
                minion.getType().getName(),
                minion.getOwnerUUID(),
                linkedChestId,
                minion.getDirection(),
                minion.getLevel(),
                minion.getStorage(),
                minion.getActionAmount(),
                tool,
                minion.getCharge()
            )
            markDirty()
        }
    }

    override fun deleteMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        synchronized(lock) {
            val stored = minions.remove(minion.getLocationId()) ?: return

            removeLocationIfUnused(minion.getLocationId())
            if (stored.chestLocationId != 0) {
                removeLocationIfUnused(stored.chestLocationId)
            }
            markDirty()
        }
    }

    override fun getMinionAmount(uuid: UUID): Int {
        synchronized(lock) {
            return minions.values.count { it.owner == uuid }
        }
    }

    override fun isMinion(location: Location): Boolean {
        val key = StoredLocation(location.world?.name ?: "---", location.blockX, location.blockY, location.blockZ)
        synchronized(lock) {
            val id = locationIds[key] ?: return false
            return minions.containsKey(id)
        }
    }

    override fun islandPlace(island: String) {
        synchronized(lock) {
            islands[island] = (islands[island] ?: 0) + 1
            markDirty()
        }
    }

    override fun islandBreak(island: String) {
        synchronized(lock) {
            val placed = islands[island] ?: return
            islands[island] = placed - 1
            markDirty()
        }
    }

    override fun islandReset(island: String) {
        synchronized(lock) {
            if (!islands.containsKey(island)) {
                return
            }

            islands[island] = 0
            markDirty()
        }
    }

    override fun getIsland(island: String): Int {
        synchronized(lock) {
            return islands[island] ?: 0
        }
    }

    override fun addUser(uuid: UUID, name: String) {
        synchronized(lock) {
            if (users.containsKey(uuid)) {
                return
            }

            users[uuid] = StoredUser(name, 0)
            markDirty()
        }
    }

    override fun addExtraSlot(user: UUID, amount: Int) {
        synchronized(lock) {
            val stored = users[user] ?: return
            stored.islandSlots += amount
            markDirty()
        }
    }

    override fun getExtraSlots(user: UUID): Int {
        synchronized(lock) {
            return users[user]?.islandSlots ?: 0
        }
    }

    override fun disable() {
        saveExecutor.shutdown()
        try {
            saveExecutor.awaitTermination(10, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        if (loaded) {
            save()
        }
    }

    private fun registerLocation(id: Int, location: StoredLocation) {
        locations[id] = location
        locationIds[location] = id
        if (!worlds.contains(location.world)) {
            worlds.add(location.world)
        }

        if (id >= nextLocationId) {
            nextLocationId = id + 1
        }
    }

    private fun removeLocationIfUnused(id: Int) {
        if (minions.containsKey(id) || minions.values.any { it.chestLocationId == id }) {
            return
        }

        locations.remove(id)?.let { locationIds.remove(it) }
    }

    private fun markDirty() {
        if (!saveScheduled.compareAndSet(false, true)) {
            return
        }

        try {
            saveExecutor.schedule({
                saveScheduled.set(false)
                save()
            }, SAVE_DELAY_SECONDS, TimeUnit.SECONDS)
        } catch (_: RejectedExecutionException) {
            // The executor is already shut down, disable() writes the file itself
        }
    }

    private fun save() {
        val yaml = YamlConfiguration()
        synchronized(lock) {
            yaml.set("next-location-id", nextLocationId)

            for ((id, location) in locations) {
                val section = yaml.createSection("locations.$id")
                section.set("world", location.world)
                section.set("x", location.x)
                section.set("y", location.y)
                section.set("z", location.z)
            }

            for ((uuid, user) in users) {
                val section = yaml.createSection("users.$uuid")
                section.set("name", user.name)
                section.set("island-slots", user.islandSlots)
            }

            for ((id, minion) in minions) {
                val section = yaml.createSection("minions.$id")
                section.set("type", minion.type)
                section.set("owner", minion.owner.toString())
                if (minion.chestLocationId != 0) {
                    section.set("chest-location", minion.chestLocationId)
                }
                section.set("direction", minion.direction.name)
                section.set("level", minion.level)
                section.set("storage", minion.storage)
                section.set("actions", minion.actions)
                section.set("charge", minion.charge)
                if (minion.tool != null) {
                    section.set("tool", minion.tool)
                }
            }

            // Island ids can contain dots, so they can not be used as yaml keys
            yaml.set("islands", islands.map { (island, placed) -> mapOf("island" to island, "placed" to placed) })
        }

        synchronized(fileLock) {
            try {
                file.parentFile.mkdirs()
                val temp = File(file.parentFile, "${file.name}.tmp")
                Files.write(temp.toPath(), yaml.saveToString().toByteArray(StandardCharsets.UTF_8))
                try {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (exception: Exception) {
                AxMinionsPlugin.INSTANCE.logger.log(Level.SEVERE, "Could not save ${file.path}!", exception)
            }
        }
    }
}
