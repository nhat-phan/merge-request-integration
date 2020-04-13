package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons

open class CommentNode(
    val comment: Comment,
    val position: CommentPosition?
) : AbstractNode() {

    override fun updatePresentation(presentation: PresentationData) {
        presentation.setIcon(if (comment.resolved) Icons.TreeNode.ResolvedComment else Icons.TreeNode.UnresolvedComment)
        presentation.addText("${comment.author.name} ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText("@${comment.author.username} · ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        presentation.addText(DateTimeUtil.toPretty(comment.createdAt), SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

}