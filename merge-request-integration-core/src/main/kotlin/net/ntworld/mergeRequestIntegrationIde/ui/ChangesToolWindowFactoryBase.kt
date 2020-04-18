package net.ntworld.mergeRequestIntegrationIde.ui

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.ChangesToolWindowTab

open class ChangesToolWindowFactoryBase(
    private val applicationService: ApplicationService
) : ToolWindowFactory {
    override fun createToolWindowContent(ideaProject: IdeaProject, toolWindow: ToolWindow) {
        val changes = ContentFactory.SERVICE.getInstance().createContent(
            ChangesToolWindowTab(applicationService.getProjectService(ideaProject)).createComponent(),
            "Files",
            true
        )
        toolWindow.contentManager.addContent(changes)
    }
}
