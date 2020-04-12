package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ui.SimpleTextAttributes

class GeneralCommentsNode : AbstractNode() {
    override fun updatePresentation(presentation: PresentationData) {
        presentation.addText("General comments", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }
}