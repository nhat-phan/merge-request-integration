package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings

interface ConnectionConfigActionListener {

    fun initialized(index: Int, providerSettings: ProviderSettings, isShared: Boolean)

    fun idChanged(index: Int, oldId: String, providerSettings: ProviderSettings, isVerified: Boolean, isShared: Boolean)

    fun fieldsChanged(index: Int, providerSettings: ProviderSettings, isVerified: Boolean, isShared: Boolean)

    fun testClicked(index: Int, providerSettings: ProviderSettings, isShared: Boolean)

    fun deleteClicked(index: Int, providerSettings: ProviderSettings, isShared: Boolean)

    fun createNewClicked()

}
