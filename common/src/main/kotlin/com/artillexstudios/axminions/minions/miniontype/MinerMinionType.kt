package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.scheduler.impl.FoliaScheduler
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.integrations.island.SuperiorSkyBlock2Integration
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import dev.lone.itemsadder.api.CustomBlock
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import me.kryniowesegryderiusz.kgenerators.Main
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack

class MinerMinionType : MinionType("miner", AxMinionsPlugin.INSTANCE.getResource("minions/miner.yml")!!) {
    companion object {
        private var asyncExecutor: ExecutorService? = null
        private val smeltingRecipes = ArrayList<FurnaceRecipe>()
        private val faces = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

        init {
            Bukkit.recipeIterator().forEachRemaining {
                if (it is FurnaceRecipe) {
                    smeltingRecipes.add(it)
                }
            }
        }
    }
    private var generatorMode = false
    private val whitelist = arrayListOf<Material>()

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(getDouble("range", minion.getLevel()))
        val tool = minion.getTool()?.getEnchantmentLevel(Enchantment.DIG_SPEED)?.div(10.0) ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((getLong("speed", minion.getLevel()) * efficiency).roundToInt())

        generatorMode = getConfig().getString("break") == "generator"
        whitelist.clear()
        getConfig().getStringList("whitelist").fastFor {
            whitelist.add(Material.matchMaterial(it.uppercase(Locale.ENGLISH)) ?: return@fastFor)
        }
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

        if (minion.getLinkedInventory()?.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        var amount = 0
        var xp = 0
        when (getConfig().getString("mode").lowercase(Locale.ENGLISH)) {
            "sphere" -> {
                LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
                    if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators()
                            .isChunkFullyLoaded(location)
                    ) {
                        val gen = Main.getPlacedGenerators().getLoaded(location)
                        val possible = gen?.isBlockPossibleToMine(location) ?: false

                        if (possible) {
                            gen?.scheduleGeneratorRegeneration()
                            return@fastFor
                        }
                    }

                    val canBreak = if (generatorMode) {
                        MinionUtils.isStoneGenerator(location)
                    } else {
                        whitelist.contains(location.block.type)
                    }

                    if (canBreak) {
                        val block = location.block
                        val drops = block.getDrops(minion.getTool())
                        xp += NMSHandler.get().getExp(block, minion.getTool() ?: return)
                        drops.forEach {
                            amount += it.amount
                        }
                        minion.addToContainerOrDrop(drops)
                        block.type = Material.AIR
                        val integration = AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()
                        if (integration is SuperiorSkyBlock2Integration) {
                            integration.handleBlockBreak(block)
                        }
                    }
                }
            }

            "asphere" -> {
                if (Scheduler.get() !is FoliaScheduler) {
                    if (asyncExecutor == null) {
                        asyncExecutor = Executors.newFixedThreadPool(3)
                    }

                    asyncExecutor!!.execute {
                        LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false)
                            .fastFor { location ->
                                if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators()
                                        .isChunkFullyLoaded(location)
                                ) {
                                    val gen = Main.getPlacedGenerators().getLoaded(location)
                                    val possible = gen?.isBlockPossibleToMine(location) ?: false

                                    if (possible) {
                                        gen?.scheduleGeneratorRegeneration()
                                        return@fastFor
                                    }
                                }

                                val canBreak = if (generatorMode) {
                                    MinionUtils.isStoneGenerator(location)
                                } else {
                                    whitelist.contains(location.block.type)
                                }

                                if (canBreak) {
                                    Scheduler.get().run {
                                        val block = location.block
                                        val drops = block.getDrops(minion.getTool())
                                        xp += NMSHandler.get().getExp(block, minion.getTool() ?: return@run)
                                        drops.forEach {
                                            amount += it.amount
                                        }
                                        minion.addToContainerOrDrop(drops)
                                        block.type = Material.AIR
                                        val integration = AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()
                                        if (integration is SuperiorSkyBlock2Integration) {
                                            integration.handleBlockBreak(block)
                                        }
                                    }
                                }
                            }
                    }
                } else {
                    LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false)
                        .fastFor { location ->
                            if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators()
                                    .isChunkFullyLoaded(location)
                            ) {
                                val gen = Main.getPlacedGenerators().getLoaded(location)
                                val possible = gen?.isBlockPossibleToMine(location) ?: false

                                if (possible) {
                                    gen?.scheduleGeneratorRegeneration()
                                    return@fastFor
                                }
                            }

                            val canBreak = if (generatorMode) {
                                MinionUtils.isStoneGenerator(location)
                            } else {
                                whitelist.contains(location.block.type)
                            }

                            if (canBreak) {
                                val block = location.block
                                val drops = block.getDrops(minion.getTool())
                                xp += NMSHandler.get().getExp(block, minion.getTool() ?: return)
                                drops.forEach {
                                    amount += it.amount
                                }
                                minion.addToContainerOrDrop(drops)
                                block.type = Material.AIR
                                val integration = AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()
                                if (integration is SuperiorSkyBlock2Integration) {
                                    integration.handleBlockBreak(block)
                                }
                            }
                        }
                }
            }

            "line" -> {
                faces.fastFor {
                    LocationUtils.getAllBlocksFacing(minion.getLocation(), minion.getRange(), it).fastFor { location ->
                        if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators()
                                .isChunkFullyLoaded(location)
                        ) {
                            val gen = Main.getPlacedGenerators().getLoaded(location)
                            val possible = gen?.isBlockPossibleToMine(location) ?: false

                            if (possible) {
                                gen?.scheduleGeneratorRegeneration()
                                return@fastFor
                            }
                        }

                        val canBreak = if (generatorMode) {
                            MinionUtils.isStoneGenerator(location)
                        } else {
                            whitelist.contains(location.block.type)
                        }

                        if (canBreak) {
                            val block = location.block
                            val drops = block.getDrops(minion.getTool())
                            xp += NMSHandler.get().getExp(block, minion.getTool() ?: return)
                            drops.forEach { item ->
                                amount += item.amount
                            }
                            minion.addToContainerOrDrop(drops)
                            block.type = Material.AIR
                            val integration = AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()
                            if (integration is SuperiorSkyBlock2Integration) {
                                integration.handleBlockBreak(block)
                            }
                        }
                    }
                }
            }

            "face" -> {
                LocationUtils.getAllBlocksFacing(minion.getLocation(), minion.getRange(), minion.getDirection().facing)
                    .fastFor { location ->
                        if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators()
                                .isChunkFullyLoaded(location)
                        ) {
                            val gen = Main.getPlacedGenerators().getLoaded(location)
                            val possible = gen?.isBlockPossibleToMine(location) ?: false

                            if (possible) {
                                gen?.scheduleGeneratorRegeneration()
                                return@fastFor
                            }
                        }

                        if (AxMinionsPlugin.integrations.itemsAdderIntegration) {
                            val block = CustomBlock.byAlreadyPlaced(location.block)
                            if (block !== null) {
                                val drops = block.getLoot(minion.getTool(), false)
                                drops.forEach {
                                    amount += it.amount
                                }
                                minion.addToContainerOrDrop(drops)
                                block.remove()
                                return@fastFor
                            }
                        }

                        val canBreak = if (generatorMode) {
                            MinionUtils.isStoneGenerator(location)
                        } else {
                            whitelist.contains(location.block.type)
                        }

                        if (canBreak) {
                            val block = location.block
                            val drops = block.getDrops(minion.getTool())
                            xp += NMSHandler.get().getExp(block, minion.getTool() ?: return)
                            drops.forEach {
                                amount += it.amount
                            }

                            minion.addToContainerOrDrop(drops)
                            block.type = Material.AIR
                            val integration = AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()
                            if (integration is SuperiorSkyBlock2Integration) {
                                integration.handleBlockBreak(block)
                            }
                        }
                    }
            }
        }

        val coerced =
            (minion.getStorage() + xp).coerceIn(0.0, minion.getType().getLong("storage", minion.getLevel()).toDouble())
        minion.setStorage(coerced)
        minion.setActions(minion.getActionAmount() + amount)
        minion.damageTool(amount)
    }
}