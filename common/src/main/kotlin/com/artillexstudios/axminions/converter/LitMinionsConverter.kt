package com.artillexstudios.axminions.converter

import com.artillexstudios.axapi.serializers.Serializers
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.minions.Minion
import java.sql.Connection
import java.sql.DriverManager
import java.util.Locale
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack


class LitMinionsConverter : Converter {
    private lateinit var connection: Connection

    override fun convert() {
        Bukkit.getConsoleSender()
            .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions-Converter] <white>Converting from <green>LitMinions..."))
        val url = "jdbc:sqlite:" + "plugins/LitMinions/minions.db"
        try {
            connection = DriverManager.getConnection(url) ?: return
        } catch (exception: Exception) {
            Bukkit.getConsoleSender()
                .sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions-Converter] <#FF0000>FAILED! Database not found, or corrupted!"))
            return
        }
        var skipped = 0
        var loaded = 0

        connection.use {
            connection.prepareStatement("SELECT * FROM `minions_data`").use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val location = Serializers.LOCATION.deserialize("${resultSet.getString("minionworld")};${resultSet.getString("minionx")};${resultSet.getString("miniony")};${resultSet.getString("minionz")};0;0")
                        val direction = Direction.valueOf(resultSet.getString("minionface").uppercase(Locale.ENGLISH))
                        val level = resultSet.getInt("level")
                        val type = MinionTypes.valueOf(resultSet.getString("miniontype").lowercase(Locale.ENGLISH)) ?: continue
                        val uuid = UUID.fromString(resultSet.getString("owner"))
                        val storage = resultSet.getDouble("exp")
                        val statistics = resultSet.getLong("stat")
                        val chest = "${resultSet.getString("chestworld")};${resultSet.getString("chestx")};${resultSet.getString("chesty")};${resultSet.getString("chestz")};0;0"

                        var chestLocation: Location? = null
                        if (resultSet.getString("chestworld") == null) {
                            chestLocation = Serializers.LOCATION.deserialize(chest)
                        }

                        if (location.world == null || (chestLocation != null && chestLocation.world == null) || AxMinionsPlugin.dataHandler.isMinion(location)) {
                            skipped++
                            continue
                        } else {
                            loaded++
                        }

                        val locationId = AxMinionsPlugin.dataHandler.getLocationID(location)
                        val chestLocationId = if (chestLocation != null) AxMinionsPlugin.dataHandler.getLocationID(chestLocation) else 0
                        val minion = Minion(location, uuid, Bukkit.getOfflinePlayer(uuid), type, level, ItemStack(Material.AIR), chestLocation, direction, statistics, storage, locationId, chestLocationId)
                        minion.setTicking(true)
                        AxMinionsPlugin.dataHandler.saveMinion(minion)
                    }
                }
            }
        }

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("<#33FF33>[AxMinions-Converter] <white>Converting done! Loaded: $loaded minion, skipped: $skipped minion!"))
    }
}