package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition

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

    fun makeFileLine(
        parent: FileNode,
        path: String,
        line: Int,
        count: Int,
        position: CommentPosition,
        showOpenDiffViewDescription: Boolean
    ): FileLineNode {
        val node = FileLineNode(path, line, position, count, showOpenDiffViewDescription)
        parent.add(node)
        return node
    }
}