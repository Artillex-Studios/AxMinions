package com.artillexstudios.axminions.nms.v1_18_R2

import com.artillexstudios.axminions.api.minions.Minion
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object LootHandler {

    fun generateFishingLoot(minion: Minion, waterLocation: Location): List<ItemStack> {
        val nmsItem: net.minecraft.world.item.ItemStack = if (minion.getTool() == null) {
            net.minecraft.world.item.ItemStack.EMPTY
        } else {
            CraftItemStack.asNMSCopy(minion.getTool())
        }

        val level = (minion.getLocation().world as CraftWorld).handle

        val lootparams = LootContext.Builder(level).withParameter(
            LootContextParams.ORIGIN, Vec3(waterLocation.x, waterLocation.y, waterLocation.z)
        ).withParameter(LootContextParams.TOOL, nmsItem).withOptionalParameter(LootContextParams.THIS_ENTITY, null)
            .create(LootContextParamSets.FISHING)

        val lootTable = MinecraftServer.getServer().lootTables.get(BuiltInLootTables.FISHING)

        return lootTable.getRandomItems(lootparams).stream().map { original: net.minecraft.world.item.ItemStack? ->
            CraftItemStack.asBukkitCopy(
                original
            )
        }.toList()
    }
}
