package com.artillexstudios.axminions.nms

import com.artillexstudios.axapi.utils.Version
import com.artillexstudios.axminions.api.minions.Minion
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

interface NMSHandler {
    companion object {
        private fun resolveNmsVersion(): String {
            val serverVersion = Version.getServerVersion()
            if (serverVersion != Version.UNKNOWN) {
                return serverVersion.getNMSVersion()
            }

            val pkg = Bukkit.getServer().javaClass.`package`.name
            val fallback = pkg.substringAfterLast('.', "")
            require(fallback.startsWith("v")) {
                "Unsupported server package: $pkg"
            }
            return fallback
        }

        private val handler: NMSHandler =
            Class.forName("com.artillexstudios.axminions.nms.${resolveNmsVersion()}.NMSHandler")
                .getConstructor()
                .newInstance() as NMSHandler

        fun get(): NMSHandler {
            return handler
        }
    }

    fun attack(source: Minion, target: Entity)

    fun generateRandomFishingLoot(minion: Minion, waterLocation: Location): List<ItemStack>

    fun isAnimal(entity: Entity): Boolean

    fun getAnimalUUID(): UUID

    fun getMinion(): Minion?

    fun getExp(block: Block, itemStack: ItemStack): Int
}
