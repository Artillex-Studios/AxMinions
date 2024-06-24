package com.artillexstudios.axminions.nms.v1_18_R2

import com.artillexstudios.axminions.api.events.PreMinionDamageEntityEvent
import com.artillexstudios.axminions.api.minions.Minion
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobType
import net.minecraft.world.entity.animal.Fox
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.TridentItem
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPotionEffectEvent
import java.util.*

object DamageHandler {
    private var DUMMY_ENTITY = Fox(EntityType.FOX, (Bukkit.getWorlds().get(0) as CraftWorld).handle)
    private var minion: Minion? = null

    fun getUUID(): UUID {
        return DUMMY_ENTITY.uuid
    }

    fun getMinion(): Minion? {
        return minion
    }

    fun damage(source: Minion, entity: Entity) {
        val nmsEntity = (entity as CraftEntity).handle

        synchronized(DUMMY_ENTITY) {
            this.minion = source
            var f = 1

            val nmsItem: ItemStack
            if (source.getTool() == null) {
                nmsItem = ItemStack.EMPTY
            } else {
                nmsItem = CraftItemStack.asNMSCopy(source.getTool())

                if (nmsItem.item is SwordItem) {
                    f = (f + (nmsItem.item as SwordItem).damage).toInt()
                }

                if (nmsItem.item is AxeItem) {
                    f = (f + (nmsItem.item as AxeItem).attackDamage).toInt()
                }

                if (nmsItem.item is TridentItem) {
                    f = (f + TridentItem.BASE_DAMAGE).toInt()
                }
            }

            DUMMY_ENTITY.setItemSlot(EquipmentSlot.MAINHAND, nmsItem)

            if (!nmsEntity.isAttackable) return
            val f2 = 1.0f

            var f1 = if (nmsEntity is LivingEntity) {
                EnchantmentHelper.getDamageBonus(nmsItem, (nmsEntity).mobType)
            } else {
                EnchantmentHelper.getDamageBonus(nmsItem, MobType.UNDEFINED)
            }

            f = (f * (0.2f + f2 * f2 * 0.8f)).toInt()
            f1 *= f2

            if (f > 0.0f || f1 > 0.0f) {
                var flag3 = false
                val b0: Byte = 0
                val i = b0 + (source.getTool()?.getEnchantmentLevel(Enchantment.KNOCKBACK) ?: 0)

                f = (f * 1.5f).toInt()
                f = (f + f1).toInt()

                if (nmsItem.item is SwordItem) {
                    flag3 = true
                }

                var f3 = 0.0f
                var flag4 = false
                val j = (source.getTool()?.getEnchantmentLevel(Enchantment.FIRE_ASPECT) ?: 0)

                if (nmsEntity is LivingEntity) {
                    f3 = nmsEntity.health
                    if (j > 0 && !nmsEntity.isOnFire()) {
                        flag4 = true
                        nmsEntity.setSecondsOnFire(1, false)
                    }
                }

                val event = PreMinionDamageEntityEvent(source, entity as org.bukkit.entity.LivingEntity, f.toDouble())
                Bukkit.getPluginManager().callEvent(event)
                if (event.isCancelled) {
                    return
                }

                val flag5 = nmsEntity.hurt(DamageSource.mobAttack(DUMMY_ENTITY), f.toFloat())

                if (flag5) {
                    if (i > 0) {
                        if (nmsEntity is LivingEntity) {
                            (nmsEntity).knockback(
                                (i.toFloat() * 0.5f).toDouble(),
                                Mth.sin(source.getLocation().yaw * 0.017453292f).toDouble(),
                                (-Mth.cos(source.getLocation().yaw * 0.017453292f)).toDouble()
                            )
                        } else {
                            nmsEntity.push(
                                (-Mth.sin(source.getLocation().yaw * 0.017453292f) * i.toFloat() * 0.5f).toDouble(),
                                0.1,
                                (Mth.cos(source.getLocation().yaw * 0.017453292f) * i.toFloat() * 0.5f).toDouble()
                            )
                        }
                    }

                    if (flag3) {
                        val sweep = source.getTool()?.getEnchantmentLevel(Enchantment.SWEEPING_EDGE) ?: 0
                        val f4 =
                            1.0f + if (sweep > 0) SweepingEdgeEnchantment.getSweepingDamageRatio(sweep) else 0.0f * f
                        val list: List<LivingEntity> = (source.getLocation().world as CraftWorld).handle
                            .getEntitiesOfClass(LivingEntity::class.java, nmsEntity.boundingBox.inflate(1.0, 0.25, 1.0))
                            .filter { it !is Player }
                        val iterator: Iterator<*> = list.iterator()

                        while (iterator.hasNext()) {
                            val entityliving: LivingEntity = iterator.next() as LivingEntity

                            if ((entityliving !is ArmorStand || !(entityliving).isMarker) && source.getLocation()
                                    .distanceSquared(
                                        (entity as Entity).location
                                    ) < 9.0
                            ) {
                                val damageEvent = PreMinionDamageEntityEvent(
                                    source,
                                    entityliving.bukkitEntity as org.bukkit.entity.LivingEntity,
                                    f4.toDouble()
                                )
                                Bukkit.getPluginManager().callEvent(damageEvent)
                                if (event.isCancelled) {
                                    return
                                }

                                // CraftBukkit start - Only apply knockback if the damage hits
                                if (entityliving.hurt(DamageSource.mobAttack(DUMMY_ENTITY), f4)) {
                                    entityliving.knockback(
                                        0.4000000059604645,
                                        Mth.sin(source.getLocation().yaw * 0.017453292f).toDouble(),
                                        (-Mth.cos(source.getLocation().yaw * 0.017453292f)).toDouble()
                                    )
                                }
                                // CraftBukkit end
                            }
                        }

                        val d0 = -Mth.sin(source.getLocation().yaw * 0.017453292f).toDouble()
                        val d1 = Mth.cos(source.getLocation().yaw * 0.017453292f).toDouble()

                        if ((source.getLocation().world as CraftWorld).handle is ServerLevel) {
                            ((source.getLocation().world as CraftWorld).handle as ServerLevel).sendParticles(
                                ParticleTypes.SWEEP_ATTACK,
                                source.getLocation().x + d0,
                                source.getLocation().y + 0.5,
                                source.getLocation().z + d1,
                                0,
                                d0,
                                0.0,
                                d1,
                                0.0
                            )
                        }
                    }


                    val baneOfArthropods = source.getTool()?.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) ?: 0
                    if (nmsEntity is LivingEntity) {
                        if (baneOfArthropods > 0 && (nmsEntity.mobType === MobType.ARTHROPOD)) {
                            val p: Int = 20 + nmsEntity.random.nextInt(10 * baneOfArthropods)
                            nmsEntity.addEffect(
                                MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, p, 3),
                                EntityPotionEffectEvent.Cause.ATTACK
                            )
                        }
                    }

                    if (nmsEntity is LivingEntity) {
                        val f5: Float = f3 - nmsEntity.health

                        if (j > 0) {
                            nmsEntity.setSecondsOnFire(j * 4, false)
                        }

                        if ((source.getLocation().world as CraftWorld).handle is ServerLevel && f5 > 2.0f) {
                            val k = (f5.toDouble() * 0.5).toInt()

                            ((source.getLocation().world as CraftWorld).handle).sendParticles(
                                ParticleTypes.DAMAGE_INDICATOR,
                                nmsEntity.getX(),
                                nmsEntity.getY(0.5),
                                nmsEntity.getZ(),
                                k,
                                0.1,
                                0.0,
                                0.1,
                                0.2
                            )
                        }
                    }
                } else {
                    if (flag4) {
                        nmsEntity.clearFire()
                    }
                }
            }

            this.minion = null
        }
    }
}
