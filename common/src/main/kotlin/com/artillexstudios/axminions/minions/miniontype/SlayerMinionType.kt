package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable

class SlayerMinionType : MinionType("slayer", AxMinionsPlugin.INSTANCE.getResource("minions/slayer.yml")!!) {

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

        Warnings.remove(minion, Warnings.NO_TOOL)

        minion.getLocation().world!!.getNearbyEntities(minion.getLocation(), minion.getRange(), minion.getRange(), minion.getRange()).filterIsInstance<LivingEntity>().fastFor {
            if (it is Player) return@fastFor

            if (!getConfig().getBoolean("damage-animals") && NMSHandler.get().isAnimal(it)) {
                return@fastFor
            }

            if (!getConfig().getBoolean("damage-renamed") && it.customName != null) {
                return@fastFor
            }

            if (!getConfig().getBoolean("damage-tamed") && it is Tameable && it.isTamed) {
                return@fastFor
            }

            NMSHandler.get().attack(minion, it)
        }
    }
}