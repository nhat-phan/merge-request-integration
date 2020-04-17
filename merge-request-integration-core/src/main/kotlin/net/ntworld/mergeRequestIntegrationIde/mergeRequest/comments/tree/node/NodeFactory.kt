package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import com.intellij.openapi.project.Project as IdeaProject

object NodeFactory {
    fun makeRoot(): RootNode = RootNode()

    fun makeGeneralComments(root: RootNode, count: Int): GeneralCommentsNode {
        val node = GeneralCommentsNode(count)
        root.add(node)

        return node
    }

    fun makeThread(parent: Node, threadId: String, repliedCount: Int, comment: Comment): ThreadNode {
        val node = ThreadNode(threadId, repliedCount, comment, comment.position)
        parent.add(node)
        return node
    }

    fun makeComment(parent: ThreadNode, comment: Comment): CommentNode {
        val node = CommentNode(comment, parent.position)
        parent.add(node)
        return node
    }

    fun makeFile(parent: RootNode, path: String): FileNode {
        val node = FileNode(path)
        parent.add(node)
        return node
    }

    fun makeFileLine(parent: FileNode, path: String, line: Int, count: Int, position: CommentPosition): FileLineNode {
        val node = FileLineNode(path, line, position, count)
        parent.add(node)
        return node
    }

    fun applyToTreeRoot(
        projectService: ProjectService,
        providerData: ProviderData,
        root: RootNode,
        rootTreeNode: DefaultMutableTreeNode
    ) {
        rootTreeNode.removeAllChildren()
        root.children.forEach {
            rootTreeNode.add(generateMutableTreeNode(projectService, providerData, it))
        }
    }

    private fun generateMutableTreeNode(
        projectService: ProjectService,
        providerData: ProviderData,
        node: Node
    ): MutableTreeNode {
        val presentation = MyPresentableNodeDescriptor(projectService, providerData, node)
        presentation.update()
        val treeNode = DefaultMutableTreeNode(presentation)
        node.children.forEach {
            treeNode.add(generateMutableTreeNode(projectService, providerData, it))
        }

        return treeNode
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