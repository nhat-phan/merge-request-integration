package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil

class RootNodeBuilder(comments: List<Comment>) {
    private val generalComments = comments.filter {
        null === it.position
    }
    private val positionComments = comments.filter {
        null !== it.position
    }

    fun build(): RootNode {
        val root = NodeFactory.makeRoot()

        if (generalComments.isNotEmpty()) {
            buildGeneralComments(root)
        }

        if (positionComments.isNotEmpty()) {
            buildPositionComments(root)
        }

        return root
    }

    private fun buildGeneralComments(root: RootNode) {
        val generalCommentsNode = NodeFactory.makeGeneralComments(root, generalComments.size)
        buildThreadComments(generalCommentsNode, generalComments)
    }

    private fun buildThreadComments(parent: Node, comments: List<Comment>) {
        val groups = CommentUtil.groupCommentsByThreadId(comments)
        groups.forEach { (id, items) ->
            if (items.isEmpty()) {
                return@forEach
            }

            val threadNode = NodeFactory.makeThread(parent, id, items.size - 1, items.first())
            for (i in 1..items.lastIndex) {
                NodeFactory.makeComment(threadNode, items[i])
            }
        }
    }

    private fun buildPositionComments(root: RootNode) {
        val groupedByPath = CommentUtil.groupCommentsByPositionPath(positionComments)
        groupedByPath.forEach { (path, items) ->
            if (items.isEmpty()) {
                return@forEach
            }

            val fileNode = NodeFactory.makeFile(root, path)
            val groupedByLine = CommentUtil.groupCommentsByPositionLine(items)
            groupedByLine.forEach { (line, comments) ->
                if (comments.isNotEmpty()) {
                    val fileLine = NodeFactory.makeFileLine(
                        fileNode, path, line, comments.size, comments.last().position!!
                    )
                    buildThreadComments(fileLine, comments)
                }
            }
        }
    }
}