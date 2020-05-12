package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.CommentPosition

class FileLineNode(
    val path: String,
    val line: Int,
    val position: CommentPosition,
    private val count: Int,
    private val showOpenDiffViewDescription: Boolean
) : AbstractNode() {
    override val id: String = "line[$path:$line]"
    private val openDiffViewDescription = if (null !== position.newLine) "" else " (open diff view)"

    override fun updatePresentation(presentation: PresentationData) {
        presentation.addText("Line $line", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val text = if (count == 1) "comment" else "comments"
        presentation.addText(" · $count $text", SimpleTextAttributes.GRAY_ATTRIBUTES)

        if (openDiffViewDescription.isNotEmpty() && showOpenDiffViewDescription) {
            presentation.addText(openDiffViewDescription, SimpleTextAttributes.GRAY_ATTRIBUTES)
        }
    }
}