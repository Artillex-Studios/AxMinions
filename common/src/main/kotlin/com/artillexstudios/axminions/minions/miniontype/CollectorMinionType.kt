package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item

class CollectorMinionType : MinionType("collector", AxMinionsPlugin.INSTANCE.getResource("minions/collector.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.range = getDouble("range", minion.getLevel())
        val efficiency = 1.0 - (minion.getTool()?.getEnchantmentLevel(Enchantment.DIG_SPEED)?.div(10.0) ?: 0.1)
        minionImpl.nextAction = (getLong("speed", minion.getLevel()) * efficiency).roundToInt()
    }

    override fun run(minion: Minion) {
        minion.resetAnimation()
        if (minion.getLinkedChest() == null) {
            Warnings.NO_CONTAINER.display(minion)
            return
        }

        val type = minion.getLinkedChest()!!.block.type
        if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.BARREL) {
            Warnings.NO_CONTAINER.display(minion)
            minion.setLinkedChest(null)
            return
        }

        if (minion.getLinkedInventory() == null) {
            Warnings.NO_CONTAINER.display(minion)
            minion.setLinkedChest(null)
            return
        }

        Warnings.remove(minion)

        val entities = minion.getLocation().world?.getNearbyEntities(
            minion.getLocation(),
            minion.getRange(),
            minion.getRange(),
            minion.getRange()
        )

        entities?.filterIsInstance<Item>()?.forEach { item ->
            if (minion.getLinkedInventory()?.firstEmpty() == -1) {
                Warnings.CONTAINER_FULL.display(minion)
                return
            }

            //TODO: Add to inventory and remove items
        }
    }
}