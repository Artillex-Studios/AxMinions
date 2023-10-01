package com.artillexstudios.axminions.api.minions

import com.artillexstudios.axapi.entity.impl.PacketEntity
import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.warnings.Warning
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

interface Minion {

    fun getType(): MinionType

    fun spawn()

    fun tick()

    fun getLocation(): Location

    fun updateInventory(inventory: Inventory)

    fun openInventory(player: Player)

    fun getAsItem(): ItemStack

    fun getLevel(): Int

    fun storeData(key: String, value: String?)

    fun setWarning(warning: Warning?)

    fun getWarning(): Warning?

    fun setWarningHologram(hologram: Hologram?)

    fun getWarningHologram(): Hologram?

    fun getOwner(): OfflinePlayer?

    fun getOwnerUUID(): UUID

    fun setTool(tool: ItemStack)

    fun getTool(): ItemStack?

    fun getEntity(): PacketEntity

    fun setLevel(level: Int)

    fun getData(key: String): String?

    fun hasData(key: String): Boolean

    fun getNextAction(): Int

    fun getRange(): Double

    fun resetAnimation()

    fun animate()

    fun setLinkedChest(location: Location?)

    fun getLinkedChest(): Location?

    fun serializeExtraData(): String

    fun setDirection(direction: Direction)

    fun getDirection(): Direction
}