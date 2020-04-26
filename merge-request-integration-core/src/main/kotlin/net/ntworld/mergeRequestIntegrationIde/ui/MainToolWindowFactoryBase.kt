package net.ntworld.mergeRequestIntegrationIde.ui

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.HomeToolWindowTab

open class MainToolWindowFactoryBase(
    private val applicationServiceProvider: ApplicationServiceProvider
) : ToolWindowFactory {
    override fun createToolWindowContent(ideaProject: IdeaProject, toolWindow: ToolWindow) {
        val home = ContentFactory.SERVICE.getInstance().createContent(
            HomeToolWindowTab(applicationServiceProvider.findProjectServiceProvider(ideaProject), toolWindow).createComponent(),
            "Home",
            true
        )
        home.isCloseable = false
        toolWindow.contentManager.addContent(home)
    }
}