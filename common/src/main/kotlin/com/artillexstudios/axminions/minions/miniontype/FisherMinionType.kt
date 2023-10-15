package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class FisherMinionType : MinionType("fisher", AxMinionsPlugin.INSTANCE.getResource("minions/fisher.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(getDouble("range", minion.getLevel()))
        val efficiency = 1.0 - (minion.getTool()?.getEnchantmentLevel(Enchantment.LURE)?.div(10.0) ?: 0.1)
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

        var hasWater = false
        run breaking@{
            LocationUtils.getAllBlocksInRadius(minion.getLocation(), 2.0, false).fastFor {
                if (it.block.type != Material.WATER) return@fastFor

                hasWater = true
                return@breaking
            }
        }

        if (!hasWater) {
            Warnings.NO_WATER_NEARBY.display(minion)
            return
        }
        Warnings.remove(minion, Warnings.NO_WATER_NEARBY)

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        val loot = NMSHandler.get().generateRandomFishingLoot(minion)
        val xp = ThreadLocalRandom.current().nextInt(6) + 1

        minion.addToContainerOrDrop(loot)
        minion.setStorage(minion.getStorage() + xp)
    }
}