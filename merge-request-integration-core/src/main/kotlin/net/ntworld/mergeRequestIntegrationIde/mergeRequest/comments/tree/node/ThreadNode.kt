package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition

class ThreadNode(
    val threadId: String,
    private val repliedCount: Int,
    comment: Comment,
    position: CommentPosition?
) : CommentNode(comment, position) {

    override fun updatePresentation(presentation: PresentationData) {
        super.updatePresentation(presentation)
        if (repliedCount > 0) {
            val text = if (repliedCount == 1) " reply" else " replies"
            presentation.addText(" · $repliedCount $text", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }

}