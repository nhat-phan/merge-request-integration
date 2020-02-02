package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.internal.ProjectServiceBase

@State(name = "MergeRequestIntegrationProjectLevel", storages = [(Storage("merge-request-integration-ee.xml"))])
class ProjectServiceImpl(ideaProject: IdeaProject) : ProjectServiceBase(
    ServiceManager.getService(ApplicationServiceImpl::class.java),
    ideaProject
)

