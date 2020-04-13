package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes

class FileNode(
    val path: String,
    private val count: Int
) : AbstractNode() {
    override fun updatePresentation(presentation: PresentationData) {
        val fileName = findFileName(path)
        presentation.addText(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val text = if (count == 1) "comment" else "comments"
        presentation.addText(" ($count $text)", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)

        if (fileName != path) {
            presentation.addText(" · $path", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }

    private fun findFileName(path: String): String {
        return path.split("/").last()
    }
}
