package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType

class SellerMinionType : MinionType("seller", AxMinionsPlugin.INSTANCE.getResource("minions/seller.yml")!!) {

    override fun run(minion: Minion) {

    }
}