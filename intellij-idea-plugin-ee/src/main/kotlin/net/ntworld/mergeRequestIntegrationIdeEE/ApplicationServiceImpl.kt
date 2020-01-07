package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationServiceBase

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class ApplicationServiceImpl: ApplicationServiceBase() {
    override fun isLegal(providerData: ProviderData): Boolean {
        if (!super.isLegal(providerData)) {
            return CheckLicense.isLicensed
        }
        return true
    }
}