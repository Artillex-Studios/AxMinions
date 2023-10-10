package com.artillexstudios.axminions.api.integrations

import com.artillexstudios.axminions.api.integrations.types.EconomyIntegration
import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegrations
import com.artillexstudios.axminions.api.integrations.types.StackerIntegration

interface Integrations {

    fun getStackerIntegration(): StackerIntegration

    fun getPricesIntegration(): PricesIntegration

    fun getEconomyIntegration(): EconomyIntegration

    fun getProtectionIntegration(): ProtectionIntegrations

    fun reload()

    fun register(integration: Integration)

    fun deregister(integration: Integration)
}