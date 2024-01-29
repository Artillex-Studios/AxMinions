package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.cache.Caches
import com.artillexstudios.axminions.minions.MinionTicker
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class FarmerMinionType : MinionType("farmer", AxMinionsPlugin.INSTANCE.getResource("minions/farmer.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(getDouble("range", minion.getLevel()))
        val tool = minion.getTool()?.getEnchantmentLevel(Enchantment.DIG_SPEED)?.div(10.0) ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((getLong("speed", minion.getLevel()) * efficiency).roundToInt())
    }

    override fun run(minion: Minion) {
        if (minion.getLinkedInventory() != null && minion.getLinkedInventory()?.firstEmpty() != -1) {
            Warnings.remove(minion, Warnings.CONTAINER_FULL)
        }

        if (minion.getLinkedChest() != null) {
            val type = minion.getLinkedChest()!!.block.type
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

        Warnings.remove(minion, Warnings.NO_TOOL)

        var size = 0
        LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
            val block = Caches.get(location.world!!)?.get(location.x.toInt(), location.y.toInt(), location.z.toInt())
            if (block !is Material) return@fastFor
            val locationBlock = location.block
            val drops = arrayListOf<ItemStack>()

            when (block) {
                Material.CACTUS, Material.SUGAR_CANE, Material.BAMBOO -> {
                    MinionUtils.getPlant(locationBlock).fastFor {
                        val blockDrops = it.getDrops(minion.getTool())
                        blockDrops.forEach { itemStack ->
                            size += itemStack.amount
                        }
                        drops.addAll(blockDrops)
                        it.type = Material.AIR
                    }
                }

                Material.MELON, Material.PUMPKIN -> {
                    val blockDrops = locationBlock.getDrops(minion.getTool())
                    blockDrops.forEach { itemStack ->
                        size += itemStack.amount
                    }
                    drops.addAll(blockDrops)
                    locationBlock.type = Material.AIR
                }

                Material.COCOA_BEANS, Material.COCOA, Material.NETHER_WART, Material.WHEAT, Material.CARROTS, Material.BEETROOTS, Material.POTATOES -> {
                    val ageable = locationBlock.blockData as Ageable
                    if (ageable.age != ageable.maximumAge) return@fastFor
                    val blockDrops = locationBlock.getDrops(minion.getTool())
                    blockDrops.forEach { itemStack ->
                        size += itemStack.amount
                    }
                    drops.addAll(blockDrops)
                    ageable.age = 0
                    locationBlock.blockData = ageable
                }

                Material.SWEET_BERRY_BUSH -> {
                    val ageable = locationBlock.blockData as Ageable
                    if (ageable.age != ageable.maximumAge) return@fastFor
                    val blockDrops = locationBlock.getDrops(minion.getTool())
                    blockDrops.forEach { itemStack ->
                        size += itemStack.amount
                    }
                    drops.addAll(blockDrops)
                    ageable.age = 1
                    locationBlock.blockData = ageable
                }

                else -> return@fastFor
            }

            minion.addToContainerOrDrop(drops)
            minion.damageTool(size)
            minion.setActions(minion.getActionAmount() + size)
        }
    }
}
