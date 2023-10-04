package com.artillexstudios.axminions.data

import com.artillexstudios.axapi.serializers.Serializers
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.data.DataHandler
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.h2.jdbc.JdbcConnection
import java.sql.Connection
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

        connection.prepareStatement("CREATE TABLE IF NOT EXISTS `axminions_data`(`location` VARCHAR(256) NOT NULL, `owner` VARCHAR(36) NOT NULL, `linked-chest-location` VARCHAR(256) DEFAULT NULL, `extra_data` CLOB DEFAULT NULL, `direction` TINYINT NOT NULL DEFAULT '0', `type` VARCHAR(64) NOT NULL, `level` SMALLINT NOT NULL DEFAULT '1', `tool` CLOB DEFAULT NULL, PRIMARY KEY(`location`));")
            .use { preparedStatement ->
                preparedStatement.executeUpdate()
            }
    }

    override fun loadMinionsOfType(minionType: MinionType) {
        connection.prepareStatement("SELECT * FROM `axminions_data` WHERE `type` = ?;").use { preparedStatement ->
            preparedStatement.setString(1, minionType.getName())
            preparedStatement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    val location = resultSet.getString("location")
                    val owner = resultSet.getString("owner")
                    val direction = Direction.entries[resultSet.getInt("direction")]
                    val level = resultSet.getInt("level")
                    val tool = resultSet.getString("tool")
                    val linkedChest = resultSet.getString("linked-chest-location")
                    val extraData = resultSet.getString("extra_data")
                    val uuid = UUID.fromString(owner)
                    val ownerPlayer = Bukkit.getOfflinePlayer(uuid)

                    var linkedChestLocation: Location? = null
                    if (linkedChest != null) {
                        linkedChestLocation = Serializers.LOCATION.deserialize(linkedChest)
                    }

                    var itemStack = ItemStack(Material.AIR)
                    if (tool != null) {
                        itemStack = Serializers.ITEM_STACK.deserialize(tool)
                    }

                    com.artillexstudios.axminions.minions.Minion(
                        Serializers.LOCATION.deserialize(location),
                        uuid,
                        ownerPlayer,
                        minionType,
                        level,
                        itemStack,
                        linkedChestLocation,
                        direction,
                        extraData
                    )
                }
            }
        }
    }

    override fun saveMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        connection.prepareStatement("MERGE INTO `axminions_data`(`location`, `owner`, `linked-chest-location`, `extra_data`, `direction`, `type`, `level`, `tool`) KEY(`location`) VALUES(?,?,?,?,?,?,?,?);")
            .use { preparedStatement ->
                preparedStatement.setString(1, Serializers.LOCATION.serialize(minion.getLocation()))
                preparedStatement.setString(2, minion.getOwnerUUID().toString())
                preparedStatement.setString(
                    3,
                    when (minion.getLinkedChest()) {
                        null -> null
                        else -> Serializers.LOCATION.serialize(minion.getLinkedChest())
                    }
                )
                preparedStatement.setString(4, minion.serializeExtraData())
                preparedStatement.setInt(5, minion.getDirection().ordinal)
                preparedStatement.setString(6, minion.getType().getName())
                preparedStatement.setInt(7, minion.getLevel())
                preparedStatement.setString(8, Serializers.ITEM_STACK.serialize(minion.getTool()))

                preparedStatement.executeUpdate()
            }
    }

    override fun deleteMinion(minion: com.artillexstudios.axminions.api.minions.Minion) {
        connection.prepareStatement("DELETE FROM `axminions_data` WHERE `location` = ?;").use { preparedStatement ->
            preparedStatement.setString(1, Serializers.LOCATION.serialize(minion.getLocation()))
            preparedStatement.executeUpdate()
        }
    }

    override fun getMinionAmount(uuid: UUID): Int {
        connection.prepareStatement("SELECT COUNT(`owner`) FROM `axminions_data` WHERE `owner` = ?;")
            .use { preparedStatement ->
                preparedStatement.setString(1, uuid.toString())
                preparedStatement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt(1)
                    }
                }
            }

        return 0
    }

    override fun disable() {
        connection.close()
    }
}