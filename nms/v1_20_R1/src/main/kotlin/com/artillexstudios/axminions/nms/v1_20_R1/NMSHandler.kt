package com.artillexstudios.axminions.nms.v1_20_R1

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.nms.NMSHandler
import net.minecraft.world.entity.MobCategory
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

class NMSHandler : NMSHandler {

    override fun attack(source: Minion, target: Entity) {
        DamageHandler.damage(source, target)
    }

    override fun generateRandomFishingLoot(minion: Minion, waterLocation: Location): List<ItemStack> {
        return LootHandler.generateFishingLoot(minion, waterLocation)
    }

    override fun isAnimal(entity: Entity): Boolean {
        return (entity as CraftEntity).handle.type.category == MobCategory.CREATURE
    }
}