package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ChangesToolWindowNotifier
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.ChangesToolWindowTab

abstract class AbstractSingleMRToolWindowFactory(
    private val applicationServiceProvider: ApplicationServiceProvider
) : ToolWindowFactory {
    abstract fun getToolWindowName(): String

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val projectServiceProvider = applicationServiceProvider.findProjectServiceProvider(project)
        projectServiceProvider.messageBus.connect().subscribe(
            ChangesToolWindowNotifier.TOPIC,
            MyChangesToolWindowNotifier(
                projectServiceProvider,
                getToolWindowName()
            )
        )

        val changes = ContentFactory.SERVICE.getInstance().createContent(
            ChangesToolWindowTab(projectServiceProvider).createComponent(),
            "Files",
            true
        )
        toolWindow.contentManager.addContent(changes)
    }

    private class MyChangesToolWindowNotifier(
        val projectServiceProvider: ProjectServiceProvider,
        val toolWindowName: String
    ) : ChangesToolWindowNotifier {
        override fun requestOpenToolWindow() {
            val toolWindow = ToolWindowManager.getInstance(projectServiceProvider.project).getToolWindow(
                toolWindowName
            )
            if (null !== toolWindow) {
                toolWindow.show(null)
            }
        }

        override fun requestHideToolWindow() {
            val toolWindow = ToolWindowManager.getInstance(projectServiceProvider.project).getToolWindow(
                toolWindowName
            )
            if (null !== toolWindow) {
                toolWindow.hide(null)
            }
        }

        override fun requestShowChanges(changes: List<Change>) {
        }

        override fun requestHideChanges() {
        }
    }
}
