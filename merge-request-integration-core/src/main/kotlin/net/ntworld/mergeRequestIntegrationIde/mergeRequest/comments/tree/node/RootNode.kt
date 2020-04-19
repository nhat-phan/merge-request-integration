package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData

class RootNode : AbstractNode() {
    override val id: String = "root"

    override fun updatePresentation(presentation: PresentationData) {}
}