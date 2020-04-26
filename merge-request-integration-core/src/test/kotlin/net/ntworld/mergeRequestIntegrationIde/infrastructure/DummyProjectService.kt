package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.openapi.project.Project

class DummyProjectService(project: Project) : AbstractProjectService(project) {
    override fun getApplicationService(): ApplicationService {
        TODO("")
    }
}