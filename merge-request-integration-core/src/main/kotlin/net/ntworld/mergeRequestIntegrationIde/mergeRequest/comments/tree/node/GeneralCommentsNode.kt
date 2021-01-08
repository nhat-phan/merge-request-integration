package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequestIntegrationIde.util.TextChoiceUtil

class GeneralCommentsNode(private val totalCount: Int, private val draftCount: Int) : AbstractNode() {
    override val id: String = "general-comments"

    override fun updatePresentation(presentation: PresentationData) {
        presentation.addText("General ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        presentation.addText(
            " · " + TextChoiceUtil.commentWithDraft(totalCount, draftCount),
            SimpleTextAttributes.GRAY_ATTRIBUTES
        )
    }
}