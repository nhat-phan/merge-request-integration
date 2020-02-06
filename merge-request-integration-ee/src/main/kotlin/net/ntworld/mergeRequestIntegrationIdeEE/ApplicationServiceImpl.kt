package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationServiceBase
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class ApplicationServiceImpl: ApplicationServiceBase() {
    override fun isLegal(providerData: ProviderData): Boolean {
        if (!super.isLegal(providerData)) {
            return CheckLicense.isLicensed
        }
        return true
    }

    override fun getProjectService(project: Project): ProjectService {
        return ServiceManager.getService(project, ProjectServiceImpl::class.java)
    }
}