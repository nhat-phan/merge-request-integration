package net.ntworld.mergeRequestIntegrationIde.ui

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.ChangesToolWindowTab

open class ChangesToolWindowFactoryBase(
    private val applicationServiceProvider: ApplicationServiceProvider
) : ToolWindowFactory {
    override fun createToolWindowContent(ideaProject: IdeaProject, toolWindow: ToolWindow) {
        val changes = ContentFactory.SERVICE.getInstance().createContent(
            ChangesToolWindowTab(applicationServiceProvider.findProjectServiceProvider(ideaProject)).createComponent(),
            "Files",
            true
        )
        toolWindow.contentManager.addContent(changes)
    }
}
