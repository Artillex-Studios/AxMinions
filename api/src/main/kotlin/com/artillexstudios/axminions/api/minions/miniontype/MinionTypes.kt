package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.exception.MinionTypeAlreadyRegisteredException
import org.bukkit.NamespacedKey
import java.util.Collections

object MinionTypes {
    private val TYPES = hashMapOf<String, MinionType>()
    private val MINION_KEY = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "minion_type")
    private val LEVEL_KEY = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "level")

    @JvmStatic
    fun register(type: MinionType): MinionType {
        if (TYPES.containsKey(type.getName())) {
            throw MinionTypeAlreadyRegisteredException("A minion with type ${type.getName()} has already been registered!")
        }

        TYPES[type.getName()] = type
        type.load()
        return type
    }

    @JvmStatic
    fun unregister(key: String) {
        TYPES.remove(key)
    }

    @JvmStatic
    fun valueOf(name: String): MinionType? {
        return TYPES[name]
    }

    @JvmStatic
    fun getMinionKey(): NamespacedKey {
        return MINION_KEY
    }

    @JvmStatic
    fun getLevelKey(): NamespacedKey {
        return LEVEL_KEY
    }

    @JvmStatic
    fun getMinionTypes(): Map<String, MinionType> {
        return Collections.unmodifiableMap(TYPES)
    }
}