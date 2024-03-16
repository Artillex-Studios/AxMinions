package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.CoolDown
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.api.utils.fastFor
import java.util.Locale
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MinionInventoryListener : Listener {
    private val coolDown = CoolDown<Player>()

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

        if (coolDown.contains(player)) {
            return
        }

        coolDown.add(player, 250)

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
                minion.updateArmour()
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
            minion.updateArmour()
            val toolMeta = tool.itemMeta ?: return
            toolMeta.persistentDataContainer.remove(Keys.GUI)
            tool.setItemMeta(toolMeta)

            player.inventory.addItem(tool)
            minion.updateInventories()
            return
        }

        if (!(event.clickedInventory?.getItem(event.slot)?.hasItemMeta() ?: return)) {
            return
        }

        val meta = event.clickedInventory?.getItem(event.slot)?.itemMeta ?: return
        if (!meta.persistentDataContainer.has(Keys.GUI, PersistentDataType.STRING)) return
        val type = meta.persistentDataContainer.get(Keys.GUI, PersistentDataType.STRING)

        when (type) {
            "rotate" -> {
                when (minion.getDirection()) {
                    Direction.NORTH -> minion.setDirection(Direction.WEST)
                    Direction.EAST -> minion.setDirection(Direction.NORTH)
                    Direction.SOUTH -> minion.setDirection(Direction.EAST)
                    Direction.WEST -> minion.setDirection(Direction.SOUTH)
                }
            }

            "link" -> {
                if (minion.getLinkedChest() != null) {
                    minion.setLinkedChest(null)
                    player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_UNLINK()))
                    return
                }

                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_START()))
                LinkingListener.linking[player] = minion
                player.closeInventory()
            }

            "upgrade" -> {
                val money = minion.getType().getDouble("requirements.money", minion.getLevel() + 1)
                val actions = minion.getType().getDouble("requirements.actions", minion.getLevel() + 1)

                if (minion.getType().hasReachedMaxLevel(minion)) {
                    return
                }

                if (minion.getActionAmount() < actions) {
                    sendFail(player)
                    return
                }

                AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
                    if (it.getBalance(player) < money) {
                        sendFail(player)
                        return
                    }

                    it.takeBalance(player, money)
                }

                if (Config.UPGRADE_SOUND().isNotBlank()) {
                    player.playSound(player, Sound.valueOf(Config.UPGRADE_SOUND().uppercase(Locale.ENGLISH)), 1.0f, 1.0f)
                }

                minion.setLevel(minion.getLevel() + 1)
            }

            "statistics" -> {
                val stored = minion.getStorage()

                if (stored == 0.0) {
                    return
                }

                if (minion.getType() == MinionTypes.getMinionTypes()["seller"]) {
                    AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
                        minion.getOwner()?.let { player ->
                            it.giveBalance(player, stored)
                            minion.setStorage(0.0)
                        }
                    }
                } else {
                    player.giveExp(stored.toInt())
                    minion.setStorage(0.0)
                }
            }

            "charge" -> {
                if ((AxMinionsPlugin.integrations.getEconomyIntegration()?.getBalance(player)
                        ?: return) < Config.CHARGE_PRICE()
                ) {
                    player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_FAIL()))
                    return
                }

                AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
                    minion.getOwner()?.let { player ->
                        it.takeBalance(player, Config.CHARGE_PRICE())
                    }
                }

                val chargeSeconds = (minion.getCharge() - System.currentTimeMillis()) / 1000

                if (chargeSeconds + Config.CHARGE_AMOUNT() > Config.MAX_CHARGE() * 60L) {
                    minion.setCharge(System.currentTimeMillis() + Config.MAX_CHARGE() * 60L * 1000L)
                    return
                }

                if (minion.getCharge() < System.currentTimeMillis()) {
                    minion.setCharge(System.currentTimeMillis() + Config.CHARGE_AMOUNT() * 1000)
                } else {
                    minion.setCharge(minion.getCharge() + Config.CHARGE_AMOUNT() * 1000)
                }

                if (Messages.CHARGE().isNotBlank()) {
                    player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE()))
                }
            }
        }

        minion.updateInventories()
    }

    @EventHandler
    fun onInventoryCloseEvent(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? Minion ?: return

        holder.removeOpenInventory(event.inventory)
    }

    private fun sendFail(player: Player) {
        when (Config.UPGRADE_FAIL()) {
            "title" -> {
                player.closeInventory()
                player.sendTitle(StringUtils.formatToString(Messages.UPGRADE_FAIL()), "", 10, 70, 20)
            }

            "subtitle" -> {
                player.closeInventory()
                player.sendTitle("", StringUtils.formatToString(Messages.UPGRADE_FAIL()), 10, 70, 20)
            }

            "actionbar" -> {
                player.closeInventory()
                player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    *TextComponent.fromLegacyText(StringUtils.formatToString(Messages.UPGRADE_FAIL()))
                )
            }

            else -> {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.UPGRADE_FAIL()))
            }
        }
    }
}