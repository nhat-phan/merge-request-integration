package net.ntworld.mergeRequestIntegrationIde.ui

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegrationIde.ui.editor.EditorWatcher
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.HomeToolWindowTab

open class MainToolWindowFactoryBase : ToolWindowFactory {
    override fun createToolWindowContent(ideaProject: IdeaProject, toolWindow: ToolWindow) {
        val home = ContentFactory.SERVICE.getInstance().createContent(
            HomeToolWindowTab(ideaProject, toolWindow).createComponent(),
            "Home",
            true
        )
        home.isCloseable = false
        toolWindow.contentManager.addContent(home)
        EditorWatcher.start(ideaProject, toolWindow)
    }
}