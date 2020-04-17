package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes

class GeneralCommentsNode(private val count: Int) : AbstractNode() {
    override fun updatePresentation(presentation: PresentationData) {
        presentation.addText("General ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        val text = if (count == 1) "comment" else "comments"
        presentation.addText(" · $count $text", SimpleTextAttributes.GRAY_ATTRIBUTES)
    }
}