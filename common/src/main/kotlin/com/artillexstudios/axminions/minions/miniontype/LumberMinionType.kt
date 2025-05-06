package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.ItemStack

class LumberMinionType : MinionType("lumber", AxMinionsPlugin.INSTANCE.getResource("minions/lumber.yml")!!) {

    private val SPECIAL_CASES = mapOf(
        Material.MANGROVE_LOG to Material.MANGROVE_PROPAGULE,
        Material.CRIMSON_STEM to Material.CRIMSON_FUNGUS,
        Material.WARPED_STEM to Material.WARPED_FUNGUS
    )

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(getDouble("range", minion.getLevel()))
        val tool = minion.getTool()?.getEnchantmentLevel(Enchantment.EFFICIENCY)?.div(10.0) ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((getLong("speed", minion.getLevel()) * efficiency).roundToInt())
    }

    override fun run(minion: Minion) {
        if (minion.getLinkedInventory() != null && minion.getLinkedInventory()?.firstEmpty() != -1) {
            Warnings.remove(minion, Warnings.CONTAINER_FULL)
        }

        if (minion.getLinkedChest() != null) {
            val type = minion.getLinkedChest()!!.block.type
            if (type == Material.CHEST && minion.getLinkedInventory() !is DoubleChestInventory && hasChestOnSide(minion.getLinkedChest()!!.block)) {
                minion.setLinkedChest(minion.getLinkedChest())
            }

            if (type == Material.CHEST && minion.getLinkedInventory() is DoubleChestInventory && !hasChestOnSide(minion.getLinkedChest()!!.block)) {
                minion.setLinkedChest(minion.getLinkedChest())
            }

            if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.BARREL) {
                minion.setLinkedChest(null)
            }
        }

        if (minion.getLinkedInventory() == null) {
            minion.setLinkedChest(null)
        }

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        if (minion.getLinkedInventory()?.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        val loot = ArrayList<ItemStack>()
        LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
            MinionUtils.getTree(location.block).forEach {
                val down = it.getRelative(BlockFace.DOWN).type
                loot.addAll(it.getDrops(minion.getTool()))

                if (down == Material.DIRT || down == Material.GRASS_BLOCK || down == Material.COARSE_DIRT || down == Material.ROOTED_DIRT || down == Material.DIRT_PATH || down == Material.MUD || down == Material.MUDDY_MANGROVE_ROOTS || down == Material.PODZOL) {
                    it.type = getSaplingType(it.type)
                } else {
                    it.type = Material.AIR
                }
            }
        }

        val lootSize = loot.size
        minion.damageTool(lootSize)
        minion.setActions(minion.getActionAmount() + lootSize)
        minion.addToContainerOrDrop(loot)
    }

    private fun getSaplingType(material: Material): Material {
        SPECIAL_CASES[material]?.let { return it }

        val woodType = material.name.replace("_LOG", "")

        val saplingName = "${woodType}_SAPLING"

        return try {
            Material.valueOf(saplingName)
        } catch (e: IllegalArgumentException) {
            Material.OAK_SAPLING
        }
    }

}