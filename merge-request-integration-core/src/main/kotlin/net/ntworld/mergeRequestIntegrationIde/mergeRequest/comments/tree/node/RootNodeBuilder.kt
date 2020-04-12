package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.Comment

class RootNodeBuilder(private val comments: List<Comment>) {

    fun build(): RootNode {
        val root = NodeFactory.makeRoot()

        NodeFactory.makeGeneralComments(root)

        return root
    }

}