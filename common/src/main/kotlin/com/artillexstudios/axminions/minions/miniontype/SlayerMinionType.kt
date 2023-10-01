package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType

class SlayerMinionType : MinionType("slayer", AxMinionsPlugin.INSTANCE.getResource("minions/slayer.yml")!!) {

    override fun run(minion: Minion) {
        TODO("Not yet implemented")
    }
}