package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.utils.LocationUtils
import com.artillexstudios.axminions.utils.fastFor
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
        val efficiency = 1.0 - (minion.getTool()?.getEnchantmentLevel(Enchantment.DIG_SPEED)?.div(10.0) ?: 0.1)
        minionImpl.setNextAction((getLong("speed", minion.getLevel()) * efficiency).roundToInt())
    }

    override fun run(minion: Minion) {
        minion.resetAnimation()
        LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
            val block = location.block
            val drops = arrayListOf<ItemStack>()

            when (block.type) {
                Material.CACTUS, Material.SUGAR_CANE, Material.BAMBOO, Material.MELON, Material.PUMPKIN -> {
                    drops.addAll(block.getDrops(minion.getTool()))
                    block.type = Material.AIR
                }
                Material.COCOA_BEANS, Material.COCOA, Material.NETHER_WART, Material.WHEAT, Material.CARROTS, Material.BEETROOTS, Material.POTATOES -> {
                    val ageable = block.blockData as Ageable
                    if (ageable.age != ageable.maximumAge) return@fastFor
                    drops.addAll(block.getDrops(minion.getTool()))
                    ageable.age = 0
                    block.blockData = ageable
                }
                Material.SWEET_BERRY_BUSH -> {
                    val ageable = block.blockData as Ageable
                    if (ageable.age != ageable.maximumAge) return@fastFor
                    drops.addAll(block.getDrops(minion.getTool()))
                    ageable.age = 1
                    block.blockData = ageable
                }
                else -> return@fastFor
            }

            minion.addToContainerOrDrop(drops)
        }
    }
}