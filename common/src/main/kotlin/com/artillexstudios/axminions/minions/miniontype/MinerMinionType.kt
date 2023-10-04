package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType

class MinerMinionType : MinionType("miner", AxMinionsPlugin.INSTANCE.getResource("minions/miner.yml")!!) {

    override fun run(minion: Minion) {

    }
}