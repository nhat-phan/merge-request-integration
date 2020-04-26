package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectService

class NodeDescriptorServiceImpl(
    private val projectService: ProjectService,
    private val providerData: ProviderData
) : NodeDescriptorService {

    override fun make(node: Node): PresentableNodeDescriptor<Node> {
        val presentation = MyPresentableNodeDescriptor(projectService, providerData, node)
        presentation.update()
        return presentation
    }

    override fun findNode(input: Any?): Node? {
        return if (null !== input && input is MyPresentableNodeDescriptor) {
            input.element
        } else null
    }

    override fun isHolding(input: Any?, node: Node): Boolean {
        if (null !== input && input is MyPresentableNodeDescriptor) {
            return node.id == input.element.id
        }
        return false
    }

    private class MyPresentableNodeDescriptor(
        private val projectService: ProjectService,
        private val providerData: ProviderData,
        private val element: Node
    ) : PresentableNodeDescriptor<Node>(projectService.project, null) {
        override fun update(presentation: PresentationData) {
            element.updatePresentation(projectService, providerData, presentation)
        }

        override fun getElement(): Node = element
    }

}