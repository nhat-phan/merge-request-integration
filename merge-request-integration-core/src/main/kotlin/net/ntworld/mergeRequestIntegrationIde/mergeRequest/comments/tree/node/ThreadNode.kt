package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import net.ntworld.mergeRequest.CommentPosition

class ThreadNode(
    val threadId: String,
    val position: CommentPosition?
) : AbstractNode() {

    override fun updatePresentation(presentation: PresentationData) {
    }

}