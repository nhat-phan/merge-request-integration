package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider

abstract class SingleMRToolWindowFactoryBase(
    private val applicationServiceProvider: ApplicationServiceProvider
) : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val projectServiceProvider = applicationServiceProvider.findProjectServiceProvider(project)
        SingleMRToolWindowManager(projectServiceProvider, toolWindow)
    }
}
