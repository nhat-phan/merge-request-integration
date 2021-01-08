package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil

class RootNodeBuilder(
    comments: List<Comment>,
    private val showOpenDiffViewDescription: Boolean
) {
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
        val generalCommentsNode = NodeFactory.makeGeneralComments(
            root,
            generalComments.count(),
            generalComments.filter { it.isDraft }.count()
        )
        buildThreadComments(generalCommentsNode, generalComments)
    }

    private fun buildThreadComments(parent: Node, comments: List<Comment>) {
        val groups = CommentUtil.groupCommentsByThreadId(comments)
        groups.forEach { (id, items) ->
            if (items.isEmpty()) {
                return@forEach
            }

            val draftCount = items.filter { it.isDraft }.count() - 1
            val threadNode = NodeFactory.makeThread(parent, id, items.count() - 1, draftCount, items.first())
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

            val draftCountOfFile = items.filter { it.isDraft }.count()
            val fileNode = NodeFactory.makeFile(root, path, draftCountOfFile)
            val groupedByLine = CommentUtil.groupCommentsByPositionLine(items)
            groupedByLine.forEach { (line, comments) ->
                if (comments.isNotEmpty()) {
                    val draftCountOfFileLine = comments.filter { it.isDraft }.count()
                    val fileLine = NodeFactory.makeFileLine(
                        fileNode, path, line, comments.count(), draftCountOfFileLine, comments.last().position!!, showOpenDiffViewDescription
                    )
                    buildThreadComments(fileLine, comments)
                }
            }
        }
    }
}