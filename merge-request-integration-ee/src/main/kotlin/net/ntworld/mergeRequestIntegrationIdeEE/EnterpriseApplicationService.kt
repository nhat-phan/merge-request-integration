package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.AbstractApplicationService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectService

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class EnterpriseApplicationService: AbstractApplicationService() {
    override fun isLegal(providerData: ProviderData): Boolean {
        if (!super.isLegal(providerData)) {
            return CheckLicense.isLicensed
        }
        return true
    }

    override fun getProjectService(project: Project): ProjectService {
        val tmp = ServiceManager.getService(project, EnterpriseProjectService::class.java)

        return tmp
    }

    override fun getChangesToolWindowId(): String = "Merge Request's Changes"
}