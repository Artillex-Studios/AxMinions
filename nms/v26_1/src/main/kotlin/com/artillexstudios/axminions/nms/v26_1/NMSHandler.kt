package com.artillexstudios.axminions.nms.v26_1

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.nms.NMSHandler
import net.minecraft.world.entity.MobCategory
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.block.CraftBlockState
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.util.CraftLocation
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import java.util.UUID

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

    override fun getAnimalUUID(): UUID {
        return DamageHandler.getUUID()
    }

    override fun getMinion(): Minion? {
        return DamageHandler.getMinion()
    }

    override fun getExp(block: Block, itemStack: ItemStack): Int {
        val craftBlock = block as CraftBlock
        return craftBlock.blockState.block.getExpDrop(
            (block.state as CraftBlockState).handle,
            craftBlock.level.minecraftWorld,
            CraftLocation.toBlockPos(block.location),
            CraftItemStack.asNMSCopy(itemStack),
            true
        )
    }
}
