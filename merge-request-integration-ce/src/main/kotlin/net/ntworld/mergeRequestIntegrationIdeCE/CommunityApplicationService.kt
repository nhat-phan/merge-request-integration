package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.internal.AbstractApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class CommunityApplicationService: AbstractApplicationService() {
    override fun getProjectService(project: Project): ProjectService {
        return ServiceManager.getService(project, CommunityProjectService::class.java)
    }

    override fun getChangesToolWindowId(): String = "Merge Request's Changes CE"
}