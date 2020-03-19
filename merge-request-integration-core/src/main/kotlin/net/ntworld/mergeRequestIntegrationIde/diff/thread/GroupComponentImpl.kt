package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.ui.JBColor
import com.intellij.ui.components.Panel
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent

class GroupComponentImpl(
    private val borderTop: Boolean,
    private val providerData: ProviderData,
    private val mergeRequest: MergeRequest,
    override val id: String,
    override val comments: List<Comment>
) : GroupComponent {
    private val myPanel = Panel()

    init {
        myPanel.layout = BoxLayout(myPanel, BoxLayout.Y_AXIS)
        if (borderTop) {
            myPanel.border = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
        }

        comments.forEachIndexed { index, comment ->
            myPanel.add(
                ComponentFactory
                    .makeComment(providerData, mergeRequest, comment, if (index == 0) 0 else 1)
                    .createComponent()
            )
        }

    }

    override fun createComponent(): JComponent = myPanel

    override fun dispose() {
    }
}