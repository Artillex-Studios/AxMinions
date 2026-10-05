package com.artillexstudios.axminions.nms.v1_21_R3

import com.artillexstudios.axminions.api.minions.Minion
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object LootHandler {

    fun generateEntityLoot(minion: Minion, entity: org.bukkit.entity.Entity): List<ItemStack> {
        val nmsEntity = (entity as CraftEntity).handle
        val lootTableKey = nmsEntity.lootTable.orElse(null) ?: return emptyList()

        val level = (minion.getLocation().world as CraftWorld).handle
        val dummy = DamageHandler.getDummyEntity()
        val ownerPlayer = Bukkit.getPlayer(minion.getOwnerUUID())

        val lootParams = LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, nmsEntity)
            .withParameter(LootContextParams.ORIGIN, nmsEntity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, nmsEntity.damageSources().noAggroMobAttack(dummy))
            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, dummy)
            .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, dummy)
            .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, (ownerPlayer as? CraftPlayer)?.handle)
            .create(LootContextParamSets.ENTITY)

        val lootTable = MinecraftServer.getServer().reloadableRegistries().getLootTable(lootTableKey)

        return lootTable.getRandomItems(lootParams).stream().map { original: net.minecraft.world.item.ItemStack? ->
            CraftItemStack.asBukkitCopy(original)
        }.toList()
    }

    fun generateFishingLoot(minion: Minion, waterLocation: Location): List<ItemStack> {
        val nmsItem: net.minecraft.world.item.ItemStack = if (minion.getTool() == null) {
            net.minecraft.world.item.ItemStack.EMPTY
        } else {
            CraftItemStack.asNMSCopy(minion.getTool())
        }

        val level = (minion.getLocation().world as CraftWorld).handle

        val lootparams = LootParams.Builder(level).withParameter(
            LootContextParams.ORIGIN, Vec3(waterLocation.x, waterLocation.y, waterLocation.z)
        ).withParameter(LootContextParams.TOOL, nmsItem).withOptionalParameter(LootContextParams.THIS_ENTITY, null)
            .create(LootContextParamSets.FISHING)

        val lootTable = MinecraftServer.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);

        return lootTable.getRandomItems(lootparams).stream().map { original: net.minecraft.world.item.ItemStack? ->
            CraftItemStack.asBukkitCopy(
                original
            )
        }.toList()
    }
}
