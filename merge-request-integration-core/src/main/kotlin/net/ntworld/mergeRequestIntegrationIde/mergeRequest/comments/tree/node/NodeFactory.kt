package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
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
        val node = ThreadNode(threadId, repliedCount, comment, null)
        parent.add(node)
        return node
    }

    fun makeComment(parent: ThreadNode, comment: Comment): CommentNode {
        val node = CommentNode(comment, parent.position)
        parent.add(node)
        return node
    }

    fun makeFile(parent: RootNode, path: String, count: Int): FileNode {
        val node = FileNode(path, count)
        parent.add(node)
        return node
    }

    fun makeFileLine(parent: FileNode, path: String, line: Int, count: Int, position: CommentPosition): FileLineNode {
        val node = FileLineNode(path, line, position, count)
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
        presentation.update()
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