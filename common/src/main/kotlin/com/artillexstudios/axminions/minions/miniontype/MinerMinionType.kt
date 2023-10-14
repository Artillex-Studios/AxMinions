package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment

class MinerMinionType : MinionType("miner", AxMinionsPlugin.INSTANCE.getResource("minions/miner.yml")!!) {
    private var asyncExecutor: ExecutorService? = null
    private val faces = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

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

        Warnings.remove(minion)

        when (getConfig().getString("mode").lowercase(Locale.ENGLISH)) {
            "sphere" -> {
                LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
                    val isStoneGenerator = MinionUtils.isStoneGenerator(location)

                    if (isStoneGenerator) {
                        minion.addToContainerOrDrop(location.block.getDrops(minion.getTool()))
                        location.block.type = Material.AIR
                    }
                }
            }

            "asphere" -> {
                if (asyncExecutor == null) {
                    asyncExecutor = Executors.newFixedThreadPool(3)
                }

                asyncExecutor!!.execute {
                    LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
                        val isStoneGenerator = MinionUtils.isStoneGenerator(location)

                        if (isStoneGenerator) {
                            Scheduler.get().run {
                                minion.addToContainerOrDrop(location.block.getDrops(minion.getTool()))
                                location.block.type = Material.AIR
                            }
                        }
                    }
                }
            }

            "line" -> {
                faces.fastFor {
                    LocationUtils.getAllBlocksFacing(minion.getLocation(), minion.getRange(), it).fastFor { location ->
                        val isStoneGenerator = MinionUtils.isStoneGenerator(location)

                        if (isStoneGenerator) {
                            minion.addToContainerOrDrop(location.block.getDrops(minion.getTool()))
                            location.block.type = Material.AIR
                        }
                    }
                }
            }

            "face" -> {
                LocationUtils.getAllBlocksFacing(minion.getLocation(), minion.getRange(), minion.getDirection().facing).fastFor { location ->
                    val isStoneGenerator = MinionUtils.isStoneGenerator(location)

                    if (isStoneGenerator) {
                        minion.addToContainerOrDrop(location.block.getDrops(minion.getTool()))
                        location.block.type = Material.AIR
                    }
                }
            }
        }
    }
}