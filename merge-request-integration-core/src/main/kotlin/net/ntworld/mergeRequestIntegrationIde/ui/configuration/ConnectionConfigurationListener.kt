package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import java.util.*

interface ConnectionConfigurationListener: EventListener {

    fun connectionValidated(index: Int, providerSettings: ProviderSettings, isValid: Boolean)

    fun connectionVerified(index: Int, providerSettings: ProviderSettings, success: Boolean, project: Project?)

    fun connectionSaved(index: Int, providerSettings: ProviderSettings)

}
