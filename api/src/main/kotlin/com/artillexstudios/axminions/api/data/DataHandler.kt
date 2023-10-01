package com.artillexstudios.axminions.api.data

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import java.util.UUID

interface DataHandler {

    fun getType(): String

    fun setup()

    fun loadMinionsOfType(minionType: MinionType)

    fun saveMinion(minion: Minion)

    fun deleteMinion(minion: Minion)

    fun getMinionAmount(uuid: UUID): Int

    fun disable()
}