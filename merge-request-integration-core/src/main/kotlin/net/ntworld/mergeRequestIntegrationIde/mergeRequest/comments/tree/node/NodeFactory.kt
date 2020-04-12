package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import com.intellij.openapi.project.Project as IdeaProject

object NodeFactory {
    fun makeRoot(): RootNode = RootNode()

    fun makeGeneralComments(root: RootNode): GeneralCommentsNode {
        val node = GeneralCommentsNode()
        root.add(node)

        return node
    }

    fun makeThread(parent: GeneralCommentsNode, threadId: String): ThreadNode {
        val node = ThreadNode(threadId, null)
        parent.add(node)
        return node
    }

    fun applyToTreeRoot(ideaProject: IdeaProject, root: RootNode, rootTreeNode: DefaultMutableTreeNode) {
        rootTreeNode.removeAllChildren()
        root.children.forEach {
            rootTreeNode.add(generateMutableTreeNode(ideaProject, it))
        }
    }

    private fun generateMutableTreeNode(ideaProject: IdeaProject, node: Node): MutableTreeNode {
        val presentation = MyPresentableNodeDescriptor(ideaProject, node)
        val treeNode = DefaultMutableTreeNode(presentation)
        node.children.forEach {
            treeNode.add(generateMutableTreeNode(ideaProject, it))
        }

        return treeNode
    }

    private class MyPresentableNodeDescriptor(
        ideaProject: IdeaProject,
        private val element: Node
    ) : PresentableNodeDescriptor<Node>(ideaProject, null) {
        override fun update(presentation: PresentationData) {
            element.updatePresentation(presentation)
        }

        override fun getElement(): Node = element
    }
}