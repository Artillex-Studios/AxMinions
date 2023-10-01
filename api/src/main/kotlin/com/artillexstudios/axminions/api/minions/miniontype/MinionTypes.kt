package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axminions.api.exception.MinionTypeAlreadyRegisteredException
import java.util.Collections

object MinionTypes {
    private val TYPES = hashMapOf<String, MinionType>()

    fun register(type: MinionType): MinionType {
        if (TYPES.containsKey(type.getName())) {
            throw MinionTypeAlreadyRegisteredException("A minion with type ${type.getName()} has already been registered!")
        }

        TYPES[type.getName()] = type
        type.load()
        return type
    }

    fun unregister(key: String) {
        TYPES.remove(key)
    }

    fun getMinionTypes(): Map<String, MinionType> {
        return Collections.unmodifiableMap(TYPES)
    }
}