package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.CommentPosition

class FileLineNode(
    val path: String,
    val line: Int,
    val position: CommentPosition,
    private val count: Int
) : AbstractNode() {
    override fun updatePresentation(presentation: PresentationData) {
        presentation.addText("Line $line", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val text = if (count == 1) "comment" else "comments"
        presentation.addText(" · $count $text", SimpleTextAttributes.GRAY_ATTRIBUTES)
    }
}