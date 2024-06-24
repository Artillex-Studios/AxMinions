package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.DoubleChestInventory

class SellerMinionType : MinionType("seller", AxMinionsPlugin.INSTANCE.getResource("minions/seller.yml")!!) {

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

        if (minion.getLinkedChest() == null) {
            Warnings.NO_CONTAINER.display(minion)
            return
        }

        val type = minion.getLinkedChest()!!.block.type
        if (type == Material.CHEST && minion.getLinkedInventory() !is DoubleChestInventory && hasChestOnSide(minion.getLinkedChest()!!.block)) {
            minion.setLinkedChest(minion.getLinkedChest())
        }

        if (type == Material.CHEST && minion.getLinkedInventory() is DoubleChestInventory && !hasChestOnSide(minion.getLinkedChest()!!.block)) {
            minion.setLinkedChest(minion.getLinkedChest())
        }

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

        Warnings.remove(minion, Warnings.NO_CONTAINER)

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        if (AxMinionsPlugin.integrations.getPricesIntegration() === null) {
            return
        }

        for (it in minion.getLinkedInventory()!!.contents) {
            if (it == null || it.type == Material.AIR) {
                continue
            }

            var price = AxMinionsPlugin.integrations.getPricesIntegration()!!.getPrice(it)

            if (price <= 0) {
                if (getConfig().get("delete-unsellable")) {
                    it.amount = 0
                }
                continue
            }

            price *= getDouble("multiplier", minion.getLevel())

            if (minion.getStorage() + price > getDouble("storage", minion.getLevel())) {
                continue
            }

            minion.setActions(minion.getActionAmount() + it.amount)
            minion.damageTool()
            minion.setStorage(minion.getStorage() + price)
            it.amount = 0
        }
    }
}