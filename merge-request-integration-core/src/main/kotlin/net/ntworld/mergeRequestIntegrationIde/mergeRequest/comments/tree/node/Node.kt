package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

interface Node {
    val id: String

    var parent: Node?

    val children: List<Node>

    val childCount
        get() = children.size

    fun add(node: Node)

    fun updatePresentation(presentation: PresentationData)

    fun updatePresentation(projectService: ProjectService, providerData: ProviderData, presentation: PresentationData) {
        updatePresentation(presentation)
    }
}