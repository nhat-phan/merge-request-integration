package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.infrastructure.AbstractProjectService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationService

@State(name = "MergeRequestIntegrationProjectLevel", storages = [(Storage("merge-request-integration-ee.xml"))])
class EnterpriseProjectService(ideaProject: IdeaProject) : AbstractProjectService(ideaProject) {

    init {
        bindDataProviderForNotifiers()
    }

    override fun getApplicationService(): ApplicationService {
        return ServiceManager.getService(EnterpriseApplicationService::class.java)
    }
}

