package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.util.ui.tree.TreeUtil
import net.ntworld.mergeRequest.MergeRequestInfo
import java.lang.NullPointerException
import javax.swing.JTree
import javax.swing.tree.*
import kotlin.math.min

class NodeSyncManagerImpl(
    private val nodeDescriptorService: NodeDescriptorService
) : NodeSyncManager {
    override fun sync(mergeRequestInfo: MergeRequestInfo, root: RootNode, tree: SyncedTree) {
        val expandingNodes = mutableSetOf<String>()
        val selectedNodeToTopIds = mutableSetOf<String>()
        val selectedTreeNode = tree.selectedTreeNode()
        if (null !== selectedTreeNode) {
            val node = nodeDescriptorService.findNode(selectedTreeNode.userObject)
            var currentNode: Node? = node
            while (null !== currentNode) {
                selectedNodeToTopIds.add(currentNode.id)
                currentNode = currentNode.parent
            }
        }

        syncStructure(root, tree.treeRoot) { node, treeNode ->
            if (tree.isExpand(treeNode)) {
                expandingNodes.add(node.id)
            }
        }

        tree.nodeStructureChanged(tree.treeRoot)
        loopStructure(tree.treeRoot) { node, treeNode ->
            if (expandingNodes.contains(node.id)) {
                tree.expand(treeNode)
            }
            if (selectedNodeToTopIds.contains(node.id)) {
                tree.select(treeNode)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loopStructure(treeNode: DefaultMutableTreeNode, visitor: (Node, DefaultMutableTreeNode) -> Unit) {
        visitor((treeNode.userObject as PresentableNodeDescriptor<Node>).element, treeNode)
        val children = treeNode.children();
        if (children != null) {
            while (children.hasMoreElements()) {
                val child = children.nextElement()
                loopStructure(child as DefaultMutableTreeNode, visitor)
            }
        }
    }

    override fun makeSyncedTree(
        tree: JTree, treeModel: DefaultTreeModel, treeRoot: DefaultMutableTreeNode
    ): SyncedTree {
        return MySyncedTree(tree, treeModel, treeRoot)
    }

    fun syncStructure(
        parent: Node,
        treeNode: DefaultMutableTreeNode,
        visitor: ((Node, DefaultMutableTreeNode) -> Unit)
    ) {
        visitor(parent, treeNode)
        if (!nodeDescriptorService.isHolding(treeNode.userObject, parent)) {
            treeNode.userObject = nodeDescriptorService.make(parent)
        }

        val treeNodeChildren = treeNode.children().toList()
        val treeNodeChildCount = treeNodeChildren.size
        val index = min(treeNodeChildCount, parent.childCount)
        for (i in 0 until index) {
            val treeNodeChild = treeNodeChildren[i]
            syncStructure(parent.children[i], treeNodeChild as DefaultMutableTreeNode, visitor)
        }

        if (treeNodeChildCount < parent.childCount && index < parent.childCount) {
            for (i in index until parent.childCount) {
                val child = parent.children[i]
                val userObject = nodeDescriptorService.make(child)
                val childTreeNode = DefaultMutableTreeNode(userObject)
                syncStructure(child, childTreeNode, visitor)
                treeNode.add(childTreeNode)
            }
            return
        }

        if (treeNodeChildCount > parent.childCount && index < treeNode.childCount) {
            for (i in index until treeNodeChildCount) {
                // Always remove index because after removing 1 item the list is has 1 item less, then index
                // is the same :D
                treeNode.remove(index)
            }
            return
        }
    }

    private class MySyncedTree(
        private val tree: JTree,
        private val treeModel: DefaultTreeModel,
        override val treeRoot: DefaultMutableTreeNode
    ) : SyncedTree {
        private fun findPath(treeNode: TreeNode): TreePath? {
            return try {
                TreeUtil.getPath(treeRoot, treeNode)
            } catch (exception: NullPointerException) {
                null
            }
        }

        override fun isExpand(treeNode: TreeNode): Boolean {
            val path = findPath(treeNode)
            return if (null !== path) tree.isExpanded(path) else false
        }

        override fun selectedTreeNode(): DefaultMutableTreeNode? {
            return if (null === tree.selectionPath) {
                null
            } else {
                tree.selectionPath!!.lastPathComponent as DefaultMutableTreeNode?
            }
        }

        override fun select(treeNode: TreeNode) {
            val path = findPath(treeNode)
            if (null !== path) {
                tree.selectionPath = path
            }
        }

        override fun expand(treeNode: TreeNode) {
            val path = findPath(treeNode)
            if (null !== path) {
                tree.expandPath(path)
            }
        }

        override fun nodeStructureChanged(treeNode: TreeNode) {
            treeModel.nodeStructureChanged(treeNode)
        }
    }
}