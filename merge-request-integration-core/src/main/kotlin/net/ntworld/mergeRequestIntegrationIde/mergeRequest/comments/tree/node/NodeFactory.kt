package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition

object NodeFactory {
    fun makeRoot(): RootNode = RootNode()

    fun makeGeneralComments(root: RootNode, totalCount: Int, draftCount: Int): GeneralCommentsNode {
        val node = GeneralCommentsNode(totalCount, draftCount)
        root.add(node)

        return node
    }

    fun makeThread(parent: Node, threadId: String, repliedCount: Int, draftCount: Int, comment: Comment): ThreadNode {
        val node = ThreadNode(threadId, repliedCount, draftCount, comment, comment.position)
        parent.add(node)
        return node
    }

    fun makeComment(parent: ThreadNode, comment: Comment): CommentNode {
        val node = CommentNode(comment, parent.position)
        parent.add(node)
        return node
    }

    fun makeFile(parent: RootNode, path: String, draftCount: Int): FileNode {
        val node = FileNode(path, draftCount)
        parent.add(node)
        return node
    }

    fun makeFileLine(
        parent: FileNode,
        path: String,
        line: Int,
        totalCount: Int,
        draftCount: Int,
        position: CommentPosition,
        showOpenDiffViewDescription: Boolean
    ): FileLineNode {
        val node = FileLineNode(path, line, position, totalCount, draftCount, showOpenDiffViewDescription)
        parent.add(node)
        return node
    }
}