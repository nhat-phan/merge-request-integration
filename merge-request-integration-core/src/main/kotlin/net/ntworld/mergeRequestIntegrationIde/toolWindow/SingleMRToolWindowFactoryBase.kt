package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.SingleMRToolWindowNotifier
import net.ntworld.mergeRequestIntegrationIde.toolWindow.internal.FilesToolWindowTabImpl

abstract class SingleMRToolWindowFactoryBase(
    private val applicationServiceProvider: ApplicationServiceProvider
) : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val projectServiceProvider = applicationServiceProvider.findProjectServiceProvider(project)
        val filesToolWindowTab = FilesToolWindowTabImpl(projectServiceProvider)
        projectServiceProvider.messageBus.connect().subscribe(
            SingleMRToolWindowNotifier.TOPIC,
            MySingleMRToolWindowNotifier(filesToolWindowTab)
        )

        val changes = ContentFactory.SERVICE.getInstance().createContent(
            filesToolWindowTab.component,
            "Files",
            true
        )
        toolWindow.contentManager.addContent(changes)
    }

    private class MySingleMRToolWindowNotifier(
        val filesToolWindowTab: FilesToolWindowTab
    ) : SingleMRToolWindowNotifier {
        override fun requestShowChanges(changes: List<Change>) {
            filesToolWindowTab.setChanges(changes)
        }

        override fun requestHideChanges() {
            filesToolWindowTab.hide()
        }
    }
}
