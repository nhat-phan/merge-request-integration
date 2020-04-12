package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

abstract class AbstractNode : Node {
    override var parent: Node? = null

    override val children: MutableList<Node> = mutableListOf()

    override fun add(node: Node) {
        node.parent = this

        children.add(node)
    }
}