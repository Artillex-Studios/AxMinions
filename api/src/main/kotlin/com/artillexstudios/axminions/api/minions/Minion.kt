package com.artillexstudios.axminions.api.minions

import com.artillexstudios.axapi.entity.impl.PacketEntity
import com.artillexstudios.axapi.hologram.Hologram
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.warnings.Warning
import java.util.UUID
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

interface Minion : InventoryHolder {

    fun getType(): MinionType

    fun spawn()

    fun tick()

    fun getLocation(): Location

    fun updateInventories()

    fun openInventory(player: Player)

    fun getAsItem(): ItemStack

    fun getLevel(): Int

    fun setActions(actions: Long)

    fun setStorage(storage: Double)

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

    fun getActionAmount(): Long

    fun getStorage(): Double

    fun getRange(): Double

    fun resetAnimation()

    fun animate()

    fun setLinkedChest(location: Location?)

    fun getLinkedChest(): Location?

    fun setDirection(direction: Direction)

    fun getDirection(): Direction

    fun remove()

    fun getLinkedInventory(): Inventory?

    fun addToContainerOrDrop(itemStack: ItemStack)

    fun addToContainerOrDrop(itemStack: Iterable<ItemStack>)

    fun updateArmour()

    fun getLocationId(): Int

    fun getChestLocationId(): Int

    fun removeOpenInventory(inventory: Inventory)
}