package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings

open class GitlabConnectionsConfigurableBase(
    settings: List<ProviderSettings>
) : AbstractConnectionsConfigurable(settings) {

    override fun getId(): String = "MRI:gitlab"

    override fun getDisplayName(): String = "Gitlab new"
    
}