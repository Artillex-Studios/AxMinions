package com.artillexstudios.axminions.api.integrations

import com.artillexstudios.axminions.api.integrations.types.*

interface Integrations {

    fun getStackerIntegration(): StackerIntegration

    fun getPricesIntegration(): PricesIntegration?

    fun getEconomyIntegration(): EconomyIntegration?

    fun getIslandIntegration(): IslandIntegration?

    fun getProtectionIntegration(): ProtectionIntegrations

    fun reload()

    fun register(integration: Integration)

    fun deregister(integration: Integration)
}