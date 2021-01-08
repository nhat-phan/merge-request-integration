package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.util.TextChoiceUtil

class ThreadNode(
    val threadId: String,
    private val repliedCount: Int,
    private val draftCount: Int,
    comment: Comment,
    position: CommentPosition?
) : CommentNode(comment, position) {
    override val id: String = "thread[${comment.id}]"

    override fun updatePresentation(presentation: PresentationData) {
        super.updatePresentation(presentation)
        val text = buildReplyAndDraftText()
        if (null !== text) {
            presentation.addText(
                " · $text",
                SimpleTextAttributes.GRAYED_ATTRIBUTES
            )
        }
    }

    private fun buildReplyAndDraftText(): String? {
        if (repliedCount > 0 && draftCount > 0) {
            return TextChoiceUtil.replyWithDraft(repliedCount, draftCount)
        }

        if (repliedCount > 0) {
            return TextChoiceUtil.reply(repliedCount)
        }

        if (draftCount > 0) {
            return TextChoiceUtil.draft(draftCount)
        }

        return null
    }
}