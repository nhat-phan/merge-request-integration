package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.openapi.project.Project

class DummyProjectServiceProvider(project: Project) : AbstractProjectServiceProvider(project) {
    override val applicationServiceProvider: ApplicationServiceProvider
        get() = TODO("Not yet implemented")
}