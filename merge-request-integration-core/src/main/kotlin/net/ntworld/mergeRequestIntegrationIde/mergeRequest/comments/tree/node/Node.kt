package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData

interface Node {
    var parent: Node?

    val children: List<Node>

    fun add(node: Node)

    fun updatePresentation(presentation: PresentationData)
}