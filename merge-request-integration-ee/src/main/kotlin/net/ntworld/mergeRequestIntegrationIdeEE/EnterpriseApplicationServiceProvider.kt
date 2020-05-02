package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.AbstractApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class EnterpriseApplicationServiceProvider: AbstractApplicationServiceProvider() {
    override fun isLegal(providerData: ProviderData): Boolean {
        if (!super.isLegal(providerData)) {
            return CheckLicense.isLicensed
        }
        return true
    }

    override fun findProjectServiceProvider(project: Project): ProjectServiceProvider {
        val service = ServiceManager.getService(project, EnterpriseProjectServiceProvider::class.java)
        registerProjectServiceProvider(service)

        return service
    }
}