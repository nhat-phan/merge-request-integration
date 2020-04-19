package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.internal.AbstractProjectService
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class DummyProjectService(project: Project) : AbstractProjectService(project) {
    override fun getApplicationService(): ApplicationService {
        TODO("")
    }
}