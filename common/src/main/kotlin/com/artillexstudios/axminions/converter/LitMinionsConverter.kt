package com.artillexstudios.axminions.converter

import com.artillexstudios.axapi.serializers.Serializers
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.minions.Direction
import java.sql.Connection
import java.sql.DriverManager
import java.util.Locale
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

        connection.use {
            connection.prepareStatement("SELECT * FROM `minions_data`").use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
//                        Bukkit.getConsoleSender().sendMessage(
//                            StringUtils.formatToString(
////                                "&#DDDDDD[AxMinions-Converter] Converting: " + resultSet.getString("minionworld") + ";" + rs.getDouble(
//                                    "minionx"
//                                ) + ";" + resultSet.getDouble("miniony") + ";" + resultSet.getDouble("minionz")
//                            )
//                        )

/*                        val location =
                            resultSet.getString("minionworld") + ";" + resultSet.getDouble("minionx") + ";" + resultSet.getDouble(
                                "miniony"*//**//*
//                            ) + ";" + rs.getDouble(
                                "minionz"
                            )*/
                        val direction: Direction =
                            Direction.valueOf(resultSet.getString("minionface").uppercase(Locale.ENGLISH))
                        val level = resultSet.getInt("level")
                        val type = resultSet.getString("miniontype").uppercase(Locale.getDefault())
                        val owner = resultSet.getString("owner")

                        val storage = resultSet.getDouble("exp")

                        val statistic = resultSet.getLong("stat")
                        val linkedChest = "${resultSet.getString("chestworld")};${resultSet.getDouble("chestx")};${
                            resultSet.getDouble("chesty")
                        };${resultSet.getDouble("chestz")};0;0"

                        var linkedChestLoc: Location? = null

                        if (resultSet.getString("chestworld") != null) {
                            linkedChestLoc = Serializers.LOCATION.deserialize(linkedChest)
                        }

                        val it = ItemStack(Material.AIR)

//                        val locationId = AxMinionsPlugin.dataHandler.getLocationID(location)
//                        val minion = Minion(location, owner, Bukkit.getOfflinePlayer(owner), minionType, 1, ItemStack(Material.AIR), null, Direction.NORTH, 0, 0.0, locationId, 0)
//                        minion.setLevel(level)
//                        minion.setActions(stats)
//                        minion.setTicking(true)
                    }
                }
            }
        }
    }
}