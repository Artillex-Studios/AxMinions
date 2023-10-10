package com.artillexstudios.axminions.integrations

import com.artillexstudios.axminions.api.integrations.types.EconomyIntegration
import com.artillexstudios.axminions.api.integrations.Integration
import com.artillexstudios.axminions.api.integrations.Integrations
import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegration
import com.artillexstudios.axminions.api.integrations.types.ProtectionIntegrations
import com.artillexstudios.axminions.api.integrations.types.StackerIntegration

class Integrations : Integrations {
    private lateinit var stackerIntegration: StackerIntegration
    private lateinit var pricesIntegration: PricesIntegration
    private lateinit var economyIntegration: EconomyIntegration
    private lateinit var protectionIntegrations: ProtectionIntegrations

    override fun getStackerIntegration(): StackerIntegration {
        return stackerIntegration
    }

    override fun getPricesIntegration(): PricesIntegration {
        return pricesIntegration
    }

    override fun getEconomyIntegration(): EconomyIntegration {
        return economyIntegration
    }

    override fun getProtectionIntegration(): ProtectionIntegrations {
        return protectionIntegrations
    }

    override fun reload() {
        TODO("Not yet implemented")
    }

    override fun register(integration: Integration) {
        when (integration) {
            is StackerIntegration -> {
                stackerIntegration = integration
            }

            is ProtectionIntegration -> {
                // Hook into protection
            }

            is EconomyIntegration -> {
                economyIntegration = integration
            }

            is PricesIntegration -> {
                pricesIntegration = integration
            }
        }
    }

    override fun deregister(integration: Integration) {
        TODO("Not yet implemented")
    }
}