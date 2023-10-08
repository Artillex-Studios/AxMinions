package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.utils.fastFor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MinionInventoryListener : Listener {

    @EventHandler
    fun onInventoryDragEvent(event: InventoryDragEvent) {
        if (event.inventory.holder !is Minion) return

        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val minion = event.inventory.holder as? Minion ?: return
        if (event.clickedInventory == null) return
        event.isCancelled = true
        val player = event.whoClicked as Player

        val allowedTools = arrayListOf<Material>()
        minion.getType().getConfig().getStringList("tool.material").fastFor {
            allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
        }

        if (event.clickedInventory == player.inventory && event.currentItem != null) {
            if (event.currentItem!!.type !in allowedTools) {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.WRONG_TOOL()))
                return
            }

            if (minion.getTool()?.type != Material.AIR) {
                val current = event.currentItem!!.clone()
                val tool = minion.getTool()?.clone()
                minion.setTool(current)
                event.currentItem!!.amount = 0
                event.clickedInventory!!.addItem(tool)
            } else {
                minion.setTool(event.currentItem!!)
                event.currentItem!!.amount = 0
            }

            minion.updateInventories()
            return
        }

        if (event.slot == AxMinionsAPI.INSTANCE.getConfig().get("gui.items.item.slot")) {
            if (minion.getTool()?.type == Material.AIR) return
            if (player.inventory.firstEmpty() == -1) {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.ERROR_INVENTORY_FULL()))
                return
            }

            val tool = minion.getTool()?.clone() ?: return
            minion.setTool(ItemStack(Material.AIR))
            val toolMeta = tool.itemMeta ?: return
            toolMeta.persistentDataContainer.remove(MinionTypes.getGuiKey())
            tool.setItemMeta(toolMeta)

            player.inventory.addItem(tool)
            return
        }

        if (!(event.clickedInventory?.getItem(event.slot)?.hasItemMeta() ?: return)) {
            return
        }

        val meta = event.clickedInventory?.getItem(event.slot)?.itemMeta ?: return
        if (!meta.persistentDataContainer.has(MinionTypes.getGuiKey(), PersistentDataType.STRING)) return
        val type = meta.persistentDataContainer.get(MinionTypes.getGuiKey(), PersistentDataType.STRING)

        when (type) {
            "rotate" -> {
                when (minion.getDirection()) {
                    Direction.NORTH -> minion.setDirection(Direction.WEST)
                    Direction.EAST ->  minion.setDirection(Direction.NORTH)
                    Direction.SOUTH ->  minion.setDirection(Direction.EAST)
                    Direction.WEST ->  minion.setDirection(Direction.SOUTH)
                }
            }

            "link" -> {
                if (minion.getLinkedChest() != null) {
                    minion.setLinkedChest(null)
                    // TODO Unlink message
                    return
                }

                LinkingListener.linking[player.uniqueId] = minion
            }
        }

        minion.updateInventories()
    }

    @EventHandler
    fun onInventoryClosEvent(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? Minion ?: return

        holder.removeOpenInventory(event.inventory)
    }
}