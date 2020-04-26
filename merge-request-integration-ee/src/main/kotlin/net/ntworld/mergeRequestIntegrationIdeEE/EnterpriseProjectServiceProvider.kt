package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.infrastructure.AbstractProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider

@State(name = "MergeRequestIntegrationProjectLevel", storages = [(Storage("merge-request-integration-ee.xml"))])
class EnterpriseProjectServiceProvider(ideaProject: IdeaProject) : AbstractProjectServiceProvider(ideaProject) {

    init {
        bindDataProviderForNotifiers()
    }

    override val applicationServiceProvider: ApplicationServiceProvider = ServiceManager.getService(
        EnterpriseApplicationServiceProvider::class.java
    )
}

