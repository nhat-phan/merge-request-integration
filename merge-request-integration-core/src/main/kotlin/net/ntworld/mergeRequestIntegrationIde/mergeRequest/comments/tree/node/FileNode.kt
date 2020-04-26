package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

class FileNode(
    val path: String
) : AbstractNode() {
    override fun updatePresentation(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData,
        presentation: PresentationData
    ) {
        super.updatePresentation(projectServiceProvider, providerData, presentation)
        presentation.setIcon(projectServiceProvider.repositoryFile.findIcon(providerData, path))
    }

    override val id: String = "file[$path]"

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
