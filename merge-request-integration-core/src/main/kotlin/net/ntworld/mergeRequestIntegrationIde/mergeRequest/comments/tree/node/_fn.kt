package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.Comment

fun Node.groupComments(): Map<String, List<Comment>> {
    val groupedComments = mutableMapOf<String, MutableList<Comment>>()
    this.children.forEach {
        if (it !is ThreadNode) {
            return@forEach
        }
        groupedComments[it.threadId] = mutableListOf(it.comment)
        it.children.forEach { node ->
            if (node is CommentNode) {
                groupedComments[it.threadId]!!.add(node.comment)
            }
        }
    }
    return groupedComments
}