package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.infrastructure.AbstractProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider

@State(name = "MergeRequestIntegrationProjectLevel", storages = [(Storage("merge-request-integration-ce.xml"))])
class CommunityProjectServiceProvider(project: Project) : AbstractProjectServiceProvider(project) {

    override val applicationServiceProvider: ApplicationServiceProvider = ServiceManager.getService(
        CommunityApplicationServiceProvider::class.java
    )

    init {
        initWithApplicationServiceProvider(applicationServiceProvider)
    }
}