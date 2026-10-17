package com.artillexstudios.axminions.data

import com.artillexstudios.axapi.serializers.Serializers
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import java.io.File
import java.sql.Connection
import java.sql.Types
import java.util.UUID

class SQLiteDataHandler : DataHandler {
    private lateinit var dataSource: HikariDataSource

    override fun getType(): String {
        return "SQLite"
    }

    override fun setup() {
        val config = HikariConfig()
        config.poolName = "AxMinions-SQLite"
        config.driverClassName = "org.sqlite.JDBC"
        config.jdbcUrl = "jdbc:sqlite:${File(AxMinionsPlugin.INSTANCE.dataFolder, "data.db").absolutePath}"
        // SQLite allows only one writer at a time, so a single connection avoids SQLITE_BUSY errors.
        // Because of that, no method may borrow a second connection while it still holds one.
        config.maximumPoolSize = 1
        config.addDataSourceProperty("journal_mode", "WAL")
        config.addDataSourceProperty("synchronous", "NORMAL")
        config.isAutoCommit = true
        dataSource = HikariDataSource(config)

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `axminions_types`(`id` INTEGER PRIMARY KEY, `name` VARCHAR(64) UNIQUE);")
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `axminions_users`(`uuid` VARCHAR(36) PRIMARY KEY, `name` VARCHAR(16), `island_slots` INT DEFAULT 0);")
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `axminions_worlds`(`id` INTEGER PRIMARY KEY, `name` VARCHAR(64) UNIQUE);")
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `axminions_locations`(`id` INTEGER PRIMARY KEY AUTOINCREMENT, `x` INT, `y` INT, `z` INT, `world_id` INT, UNIQUE(`x`, `y`, `z`, `world_id`), FOREIGN KEY(`world_id`) REFERENCES `axminions_worlds`(`id`));")
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `axminions_minions`(`id` INTEGER PRIMARY KEY, `location_id` INT UNIQUE, `chest_location_id` INT, `owner_id` VARCHAR(36), `type_id` INT, `direction` TINYINT, `level` SMALLINT, `storage` DOUBLE, `actions` BIGINT, `tool` TEXT, `charge` BIGINT DEFAULT 0, FOREIGN KEY(`location_id`) REFERENCES `axminions_locations`(`id`), FOREIGN KEY(`chest_location_id`) REFERENCES `axminions_locations`(`id`), FOREIGN KEY(`owner_id`) REFERENCES `axminions_users`(`uuid`), FOREIGN KEY(`type_id`) REFERENCES `axminions_types`(`id`));")
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS `axminions_minions_owner` ON `axminions_minions`(`owner_id`);")
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS `axminions_minions_chest` ON `axminions_minions`(`chest_location_id`);")
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `axminions_island_counter`(`island` VARCHAR(256) PRIMARY KEY, `placed` INT);")
            }
        }
    }

    override fun insertType(minionType: MinionType) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT OR IGNORE INTO `axminions_types`(`name`) VALUES(?);").use {
                it.setString(1, minionType.getName())
                it.executeUpdate()
            }
        }
    }

    override fun loadMinionsForWorld(minionType: MinionType, world: World) {
        // Minions are created after the connection is released, so their constructors can never block on the pool
        val minions = ArrayList<() -> Unit>()

        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `minions`.*, `location`.`x`, `location`.`y`, `location`.`z`, `chest`.`x` AS `chest_x`, `chest`.`y` AS `chest_y`, `chest`.`z` AS `chest_z`, `chest_world`.`name` AS `chest_world_name` FROM `axminions_minions` AS `minions` JOIN `axminions_locations` AS `location` ON `minions`.`location_id` = `location`.`id` JOIN `axminions_worlds` AS `world` ON `location`.`world_id` = `world`.`id` JOIN `axminions_types` AS `type` ON `minions`.`type_id` = `type`.`id` LEFT JOIN `axminions_locations` AS `chest` ON `minions`.`chest_location_id` = `chest`.`id` LEFT JOIN `axminions_worlds` AS `chest_world` ON `chest`.`world_id` = `chest_world`.`id` WHERE `world`.`name` = ? AND `type`.`name` = ?;").use { statement ->
                statement.setString(1, world.name)
                statement.setString(2, minionType.getName())
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val locationId = resultSet.getInt("location_id")
                        val chestLocationId = resultSet.getInt("chest_location_id")
                        val ownerId = UUID.fromString(resultSet.getString("owner_id"))
                        val direction = Direction.entries[resultSet.getByte("direction").toInt()]
                        val level = resultSet.getShort("level")
                        val storage = resultSet.getDouble("storage")
                        val actions = resultSet.getLong("actions")
                        val tool = resultSet.getString("tool")
                        val charge = resultSet.getLong("charge")

                        val location = Location(
                            world,
                            resultSet.getInt("x").toDouble(),
                            resultSet.getInt("y").toDouble(),
                            resultSet.getInt("z").toDouble()
                        )

                        var chestLocation: Location? = null
                        val chestWorld = resultSet.getString("chest_world_name")
                        if (chestLocationId != 0 && chestWorld != null) {
                            chestLocation = Location(
                                Bukkit.getWorld(chestWorld),
                                resultSet.getInt("chest_x").toDouble(),
                                resultSet.getInt("chest_y").toDouble(),
                                resultSet.getInt("chest_z").toDouble()
                            )
                        }

                        var itemStack = ItemStack(Material.AIR)
                        if (tool != null) {
                            itemStack = Serializers.ITEM_STACK.deserialize(tool)
                        }

                        minions.add {
                            com.artillexstudios.axminions.minions.Minion(
                                location,
                                ownerId,
                                Bukkit.getOfflinePlayer(ownerId),
                                minionType,
                                level.toInt(),
                                itemStack,
                                chestLocation,
                                direction,
                                actions,
                                storage,
                                locationId,
                                chestLocationId,
                                charge
                            )
                        }
                    }
                }
            }
        }

        minions.forEach { it() }
    }

    override fun getLocationID(location: Location): Int {
        val worldName = location.world?.name
        dataSource.connection.use { connection ->
            // Look the location up first, an ignored insert would still use up an AUTOINCREMENT id on every save
            val existing = findLocationId(connection, location, worldName)
            if (existing != 0) {
                return existing
            }

            connection.prepareStatement("INSERT OR IGNORE INTO `axminions_worlds`(`name`) VALUES(?);").use { statement ->
                statement.setString(1, worldName)
                statement.executeUpdate()
            }

            connection.prepareStatement("INSERT OR IGNORE INTO `axminions_locations`(`x`, `y`, `z`, `world_id`) VALUES(?, ?, ?, (SELECT `id` FROM `axminions_worlds` WHERE `name` = ?));").use { statement ->
                statement.setInt(1, location.blockX)
                statement.setInt(2, location.blockY)
                statement.setInt(3, location.blockZ)
                statement.setString(4, worldName)
                statement.executeUpdate()
            }

            return findLocationId(connection, location, worldName)
        }
    }

    private fun findLocationId(connection: Connection, location: Location, worldName: String?): Int {
        connection.prepareStatement("SELECT `id` FROM `axminions_locations` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world_id` = (SELECT `id` FROM `axminions_worlds` WHERE `name` = ?);").use { statement ->
            statement.setInt(1, location.blockX)
            statement.setInt(2, location.blockY)
            statement.setInt(3, location.blockZ)
            statement.setString(4, worldName)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt("id")
                }
            }
        }

        return 0
    }

    override fun getLocation(locationId: Int): Location? {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `location`.`x`, `location`.`y`, `location`.`z`, `world`.`name` FROM `axminions_locations` AS `location` JOIN `axminions_worlds` AS `world` ON `location`.`world_id` = `world`.`id` WHERE `location`.`id` = ?;").use { statement ->
                statement.setInt(1, locationId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val x = resultSet.getInt("x")
                        val y = resultSet.getInt("y")
                        val z = resultSet.getInt("z")

                        return Location(Bukkit.getWorld(resultSet.getString("name")), x.toDouble(), y.toDouble(), z.toDouble())
                    }

                    return null
                }
            }
        }
    }

    override fun getWorld(worldId: Int): World? {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `name` FROM `axminions_worlds` WHERE `id` = ?;").use { statement ->
                statement.setInt(1, worldId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return Bukkit.getWorld(resultSet.getString("name"))
                    }

                    return null
                }
            }
        }
    }

    override fun saveMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        val locationId = getLocationID(minion.getLocation())
        val linkedChestId = minion.getLinkedChest()?.let { getLocationID(it) }

        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO `axminions_users`(`uuid`, `name`, `island_slots`) VALUES(?, ?, 0) ON CONFLICT(`uuid`) DO UPDATE SET `name` = excluded.`name`;").use { statement ->
                statement.setString(1, minion.getOwnerUUID().toString())
                statement.setString(2, minion.getOwner()?.name ?: "---")
                statement.executeUpdate()
            }

            connection.prepareStatement("INSERT INTO `axminions_minions`(`location_id`, `chest_location_id`, `owner_id`, `type_id`, `direction`, `level`, `storage`, `actions`, `tool`, `charge`) VALUES(?, ?, ?, (SELECT `id` FROM `axminions_types` WHERE `name` = ?), ?, ?, ?, ?, ?, ?) ON CONFLICT(`location_id`) DO UPDATE SET `chest_location_id` = excluded.`chest_location_id`, `owner_id` = excluded.`owner_id`, `type_id` = excluded.`type_id`, `direction` = excluded.`direction`, `level` = excluded.`level`, `storage` = excluded.`storage`, `actions` = excluded.`actions`, `tool` = excluded.`tool`, `charge` = excluded.`charge`;")
                .use { statement ->
                    statement.setInt(1, locationId)
                    if (linkedChestId == null) {
                        statement.setNull(2, Types.INTEGER)
                    } else {
                        statement.setInt(2, linkedChestId)
                    }
                    statement.setString(3, minion.getOwnerUUID().toString())
                    statement.setString(4, minion.getType().getName())
                    statement.setByte(5, minion.getDirection().ordinal.toByte())
                    statement.setInt(6, minion.getLevel())
                    statement.setDouble(7, minion.getStorage())
                    statement.setLong(8, minion.getActionAmount())
                    if (minion.getTool() == null || minion.getTool()?.type == Material.AIR) {
                        statement.setNull(9, Types.VARCHAR)
                    } else {
                        statement.setString(9, Serializers.ITEM_STACK.serialize(minion.getTool()))
                    }
                    statement.setLong(10, minion.getCharge())
                    statement.executeUpdate()
                }
        }
    }

    override fun deleteMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        dataSource.connection.use { connection ->
            // Read the chest from the database, the minion's own id is not updated when it gets relinked
            var chestLocationId = 0
            connection.prepareStatement("SELECT `chest_location_id` FROM `axminions_minions` WHERE `location_id` = ?;").use { statement ->
                statement.setInt(1, minion.getLocationId())
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        chestLocationId = resultSet.getInt("chest_location_id")
                    }
                }
            }

            connection.prepareStatement("DELETE FROM `axminions_minions` WHERE `location_id` = ?;").use { statement ->
                statement.setInt(1, minion.getLocationId())
                statement.executeUpdate()
            }

            for (locationId in intArrayOf(minion.getLocationId(), chestLocationId)) {
                if (locationId == 0) {
                    continue
                }

                connection.prepareStatement("DELETE FROM `axminions_locations` WHERE `id` = ? AND NOT EXISTS(SELECT 1 FROM `axminions_minions` WHERE `location_id` = ? OR `chest_location_id` = ?);").use { statement ->
                    statement.setInt(1, locationId)
                    statement.setInt(2, locationId)
                    statement.setInt(3, locationId)
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun getMinionAmount(uuid: UUID): Int {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM `axminions_minions` WHERE `owner_id` = ?;").use { statement ->
                statement.setString(1, uuid.toString())
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt(1)
                    }
                }
            }
        }

        return 0
    }

    override fun isMinion(location: Location): Boolean {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT 1 FROM `axminions_minions` AS `minions` JOIN `axminions_locations` AS `location` ON `minions`.`location_id` = `location`.`id` JOIN `axminions_worlds` AS `world` ON `location`.`world_id` = `world`.`id` WHERE `location`.`x` = ? AND `location`.`y` = ? AND `location`.`z` = ? AND `world`.`name` = ?;").use { statement ->
                statement.setInt(1, location.blockX)
                statement.setInt(2, location.blockY)
                statement.setInt(3, location.blockZ)
                statement.setString(4, location.world?.name ?: "---")
                statement.executeQuery().use { resultSet ->
                    return resultSet.next()
                }
            }
        }
    }

    override fun islandPlace(island: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO `axminions_island_counter`(`island`, `placed`) VALUES(?, 1) ON CONFLICT(`island`) DO UPDATE SET `placed` = `placed` + 1;").use { statement ->
                statement.setString(1, island)
                statement.executeUpdate()
            }
        }
    }

    override fun islandBreak(island: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE `axminions_island_counter` SET `placed` = `placed` - 1 WHERE `island` = ?;").use { statement ->
                statement.setString(1, island)
                statement.executeUpdate()
            }
        }
    }

    override fun islandReset(island: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE `axminions_island_counter` SET `placed` = 0 WHERE `island` = ?;").use { statement ->
                statement.setString(1, island)
                statement.executeUpdate()
            }
        }
    }

    override fun getIsland(island: String): Int {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `placed` FROM `axminions_island_counter` WHERE `island` = ?;").use { statement ->
                statement.setString(1, island)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt("placed")
                    }
                }
            }
        }

        return 0
    }

    override fun addUser(uuid: UUID, name: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT OR IGNORE INTO `axminions_users`(`uuid`, `name`, `island_slots`) VALUES(?, ?, 0);").use { statement ->
                statement.setString(1, uuid.toString())
                statement.setString(2, name)
                statement.executeUpdate()
            }
        }
    }

    override fun addExtraSlot(user: UUID, amount: Int) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE `axminions_users` SET `island_slots` = `island_slots` + ? WHERE `uuid` = ?;").use { statement ->
                statement.setInt(1, amount)
                statement.setString(2, user.toString())
                statement.executeUpdate()
            }
        }
    }

    override fun getExtraSlots(user: UUID): Int {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `island_slots` FROM `axminions_users` WHERE `uuid` = ?;").use { statement ->
                statement.setString(1, user.toString())
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt("island_slots")
                    }

                    return 0
                }
            }
        }
    }

    override fun disable() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}
