package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

class FileNode(
    val path: String
) : AbstractNode() {
    override fun updatePresentation(
        projectService: ProjectService,
        providerData: ProviderData,
        presentation: PresentationData
    ) {
        super.updatePresentation(projectService, providerData, presentation)
        presentation.setIcon(projectService.repositoryFile.findIcon(providerData, path))
    }

    override fun updatePresentation(presentation: PresentationData) {
        val fileName = findFileName(path)
        presentation.addText(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        if (fileName != path) {
            presentation.addText(" · $path", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }

    private fun findFileName(path: String): String {
        return path.split("/").last()
    }
}
