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
import org.bukkit.inventory.ItemStack
import org.h2.jdbc.JdbcConnection
import java.sql.Connection
import java.sql.Statement
import java.sql.Types
import java.util.Properties
import java.util.UUID

class H2DataHandler : DataHandler {
    private lateinit var connection: Connection

    override fun getType(): String {
        return "H2"
    }

    override fun setup() {
        connection =
            JdbcConnection("jdbc:h2:./${AxMinionsPlugin.INSTANCE.dataFolder}/data", Properties(), null, null, false)

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS `axminions_types`(`id` BIGINT AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(64));")
            .use {
                it.executeUpdate()
            }

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS `axminions_users`(`uuid` UUID PRIMARY KEY, `name` VARCHAR(16));")
            .use {
                it.executeUpdate()
            }

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS `axminions_worlds`(`id` BIGINT AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(64));")
            .use {
                it.executeUpdate()
            }

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS `axminions_locations`(`id` BIGINT AUTO_INCREMENT PRIMARY KEY, `x` INT, `y` INT, `z` INT, `world_id` INT, FOREIGN KEY(world_id) REFERENCES `axminions_worlds`(`id`));")
            .use {
                it.executeUpdate()
            }

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS `axminions_minions`(`id` BIGINT AUTO_INCREMENT PRIMARY KEY, `location_id` INT, `chest_location_id` INT, `owner_id` UUID, `type_id` INT, `direction` TINYINT, `level` SMALLINT, `storage` DOUBLE, `actions` BIGINT, `tool` CLOB, FOREIGN KEY(`location_id`) REFERENCES `axminions_locations`(id), FOREIGN KEY(`chest_location_id`) REFERENCES `axminions_locations`(`id`), FOREIGN KEY(`owner_id`) REFERENCES `axminions_users`(`uuid`), FOREIGN KEY(`type_id`) REFERENCES `axminions_types`(`id`));")
            .use {
                it.executeUpdate()
            }
    }

    override fun insertType(minionType: MinionType) {
        connection.prepareStatement("MERGE INTO `axminions_types`(`name`) KEY(`name`) VALUES(?);").use {
            it.setString(1, minionType.getName())
            it.executeUpdate()
        }
    }

    override fun loadMinionsOfType(minionType: MinionType) {
        var typeId = 0
        connection.prepareStatement("SELECT `id` FROM `axminions_types` WHERE `name` = ?;").use { statement ->
            statement.setString(1, minionType.getName())
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    typeId = resultSet.getInt("id")
                }
            }
        }

        connection.prepareStatement("SELECT * FROM `axminions_minions` WHERE `type_id` = ?;").use { statement ->
            statement.setInt(1, typeId)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    val locationId = resultSet.getInt("location_id")
                    val chestLocationId = resultSet.getInt("chest_location_id")
                    val ownerId = resultSet.getObject("owner_id") as UUID
                    val direction = Direction.entries[resultSet.getByte("direction").toInt()]
                    val level = resultSet.getShort("level")
                    val storage = resultSet.getDouble("storage")
                    val actions = resultSet.getLong("actions")
                    val tool = resultSet.getString("tool")

                    val location = getLocation(locationId)
                    var chestLocation: Location? = null
                    if (chestLocationId != 0) {
                        chestLocation = getLocation(chestLocationId)
                    }

                    var itemStack = ItemStack(Material.AIR)
                    if (tool != null) {
                        itemStack = Serializers.ITEM_STACK.deserialize(tool)
                    }

                    com.artillexstudios.axminions.minions.Minion(
                        location!!,
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
                        chestLocationId
                    )
                }
            }
        }
    }

    override fun getLocationID(location: Location): Int {
        var worldId = 0
        connection.prepareStatement(
            "MERGE INTO `axminions_worlds`(`name`) KEY(`name`) VALUES(?);",
            Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setString(1, location.world?.name)
            statement.executeUpdate()

            statement.generatedKeys.use { resultSet ->
                if (resultSet.next()) {
                    worldId = resultSet.getInt(1)
                }
            }
        }

        connection.prepareStatement(
            "MERGE INTO `axminions_locations`(`x`, `y`, `z`, `world_id`) KEY(`x`, `y`, `z`, `world_id`) VALUES (?, ?, ?, ?);",
            Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setInt(1, location.blockX)
            statement.setInt(2, location.blockY)
            statement.setInt(3, location.blockZ)
            statement.setInt(4, worldId)
            statement.executeUpdate()

            statement.generatedKeys.use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt(1)
                }
            }
        }

        return 0
    }

    override fun getLocation(locationId: Int): Location? {
        connection.prepareStatement("SELECT `name` FROM `axminions_locations` WHERE `id` = ?").use { statement ->
            statement.setInt(1, locationId)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    val worldId = resultSet.getInt("world_id")
                    val x = resultSet.getInt("x")
                    val y = resultSet.getInt("y")
                    val z = resultSet.getInt("z")

                    return Location(getWorld(worldId), x.toDouble(), y.toDouble(), z.toDouble())
                }

                return null
            }
        }
    }

    override fun getWorld(worldId: Int): World? {
        connection.prepareStatement("SELECT `name` FROM `axminions_worlds` WHERE `id` = ?").use { statement ->
            statement.setInt(1, worldId)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return Bukkit.getWorld(resultSet.getString("name"))!!
                }

                return null
            }
        }
    }

    override fun saveMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        val locationId = getLocationID(minion.getLocation())
        var linkedChestId: Int? = null
        var userId: Int? = null
        var minionTypeId = 0

        connection.prepareStatement("SELECT * FROM `axminions_types` WHERE `name` = ?;").use {
            it.setString(1, minion.getType().getName())
            it.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    minionTypeId = resultSet.getInt("id")
                }
            }
        }

        if (minion.getLinkedChest() != null) {
            linkedChestId = getLocationID(minion.getLinkedChest()!!)
        }

        connection.prepareStatement(
            "MERGE INTO `axminions_users`(`uuid`, `name`) KEY(`uuid`) VALUES (?,?);",
            Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setObject(1, minion.getOwnerUUID())
            statement.setString(2, minion.getOwner()?.name ?: "---")
            statement.executeUpdate()

            statement.generatedKeys.use { resultSet ->
                if (resultSet.next()) {
                    if (resultSet.next()) {
                        userId = resultSet.getInt(1)
                    }
                }
            }
        }

        if (userId == null) {
            return
        }

        connection.prepareStatement("MERGE INTO `axminions_minions`(`location_id`, `chest_location_id`, `owner_id`, `type_id`, `direction`, `level`, `storage`, `actions`, `tool`) KEY(`location_id`) VALUES(?,?,?,?,?,?,?,?,?)")
            .use { statement ->
                statement.setInt(1, locationId)
                if (linkedChestId == null) {
                    statement.setNull(2, Types.INTEGER)
                } else {
                    statement.setInt(2, linkedChestId)
                }
                statement.setInt(3, userId!!)
                statement.setInt(4, minionTypeId)
                statement.setByte(5, minion.getDirection().ordinal.toByte())
                statement.setInt(6, minion.getLevel())
                statement.setDouble(7, minion.getStorage())
                statement.setLong(8, minion.getActionAmount())
                statement.setString(9, Serializers.ITEM_STACK.serialize(minion.getTool()))
                statement.executeUpdate()
            }
    }

    override fun deleteMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        connection.prepareStatement("DELETE FROM `axminions_minions` WHERE `location_id` = ?;")
            .use { preparedStatement ->
                preparedStatement.setInt(1, minion.getLocationId())
                preparedStatement.executeUpdate()
            }

        connection.prepareStatement("SELECT * FROM `axminions_minions` WHERE `location_id` = ?;").use { statement ->
            statement.setInt(1, minion.getLocationId())
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    connection.prepareStatement("DELETE FROM `axminions_locations` WHERE `id` = ?").use {
                        it.setInt(1, minion.getLocationId())
                        it.executeUpdate()
                    }
                }
            }
        }

        if (minion.getChestLocationId() != 0) {
            connection.prepareStatement("SELECT * FROM `axminions_minions` WHERE `chest_location_id` = ?;")
                .use { statement ->
                    statement.setInt(1, minion.getChestLocationId())
                    statement.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            connection.prepareStatement("DELETE FROM `axminions_locations` WHERE `id` = ?").use {
                                it.setInt(1, minion.getLocationId())
                                it.executeUpdate()
                            }
                        }
                    }
                }
        }
    }

    override fun getMinionAmount(uuid: UUID): Int {
        connection.prepareStatement("SELECT COUNT(`owner_id`) FROM `axminions_minions` WHERE `owner_id` = (SELECT `owner_id` FROM `axminions_users` WHERE `uuid` = ?);")
            .use { statement ->
                statement.setObject(1, uuid)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt(1)
                    }
                }
            }

        return 0
    }

    override fun isMinion(location: Location): Boolean {
        connection.prepareStatement("SELECT * FROM `axminions_minions` WHERE `location_id` = (SELECT `id` FROM `axminions_locations` WHERE x = ? AND y = ? AND z = ? AND `world_id` = (SELECT `id` FROM `axminions_worlds` WHERE `name` = ?));")
            .use { statement ->
                statement.setInt(1, location.blockX)
                statement.setInt(2, location.blockY)
                statement.setInt(3, location.blockZ)
                statement.setString(4, location.world?.name ?: "---")
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return true
                    }
                }
            }

        return false
    }

    override fun disable() {
        connection.close()
    }
}