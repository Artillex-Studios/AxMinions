package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.utils.Enchantments
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.ItemStack

class SellerMinionType : MinionType("seller", AxMinionsPlugin.INSTANCE.getResource("minions/seller.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(getDouble("range", minion.getLevel()))
        val tool = Enchantments.EFFICIENCY?.let { minion.getTool()?.getEnchantmentLevel(it)?.div(10.0) } ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((getLong("speed", minion.getLevel()) * efficiency).roundToInt())
    }

    override fun run(minion: Minion) {
        if (minion.getLinkedInventory() != null && !MinionUtils.isFull(minion.getLinkedInventory()!!)) {
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

            val itemMeta = it.itemMeta
            if (itemMeta is org.bukkit.inventory.meta.BlockStateMeta) {
                val blockState = itemMeta.blockState
                if (blockState is Container) {
                    val stacks = arrayOfNulls<ItemStack>(blockState.inventory.size)
                    for ((counter, stack) in blockState.inventory.contents.withIndex()) {
                        val success = sellItemStack(minion, stack)
                        if (!success) {
                            stacks[counter] = stack
                        }
                    }
                    blockState.inventory.contents = stacks
                    itemMeta.blockState = blockState
                }
            }
            it.itemMeta = itemMeta

            val success = sellItemStack(minion, it)
            if (success) {
                it.amount = 0
            }
        }
    }

    // Returns if the item should be removed
    private fun sellItemStack(minion: Minion, it: ItemStack): Boolean {
        var price = AxMinionsPlugin.integrations.getPricesIntegration()!!.getPrice(it)

        if (price <= 0) {
            return getConfig().get("delete-unsellable")
        }

        price *= getDouble("multiplier", minion.getLevel())

        if (minion.getStorage() + price > getDouble("storage", minion.getLevel())) {
            return false
        }

        minion.setActions(minion.getActionAmount() + it.amount)
        minion.damageTool()
        minion.setStorage(minion.getStorage() + price)
        return true
    }
}