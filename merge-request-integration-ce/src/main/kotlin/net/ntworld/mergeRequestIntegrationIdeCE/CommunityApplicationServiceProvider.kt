package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.infrastructure.AbstractApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class CommunityApplicationServiceProvider: AbstractApplicationServiceProvider() {
    override fun findProjectServiceProvider(project: Project): ProjectServiceProvider {
        val service = ServiceManager.getService(project, CommunityProjectServiceProvider::class.java)
        registerProjectServiceProvider(service)

        return service
    }

    override fun getChangesToolWindowId(): String = "Merge Request's Changes CE"
}