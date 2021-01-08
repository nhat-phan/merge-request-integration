package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.util.TextChoiceUtil

class FileLineNode(
    val path: String,
    val line: Int,
    val position: CommentPosition,
    private val totalCount: Int,
    private val draftCount: Int,
    private val showOpenDiffViewDescription: Boolean
) : AbstractNode() {
    override val id: String = "line[$path:$line]"
    private val openDiffViewDescription = if (null !== position.newLine) "" else " (open diff view)"

    override fun updatePresentation(presentation: PresentationData) {
        presentation.addText("Line $line", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        presentation.addText(" · " + TextChoiceUtil.commentWithDraft(totalCount, draftCount), SimpleTextAttributes.GRAY_ATTRIBUTES)

        if (openDiffViewDescription.isNotEmpty() && showOpenDiffViewDescription) {
            presentation.addText(openDiffViewDescription, SimpleTextAttributes.GRAY_ATTRIBUTES)
        }
    }
}