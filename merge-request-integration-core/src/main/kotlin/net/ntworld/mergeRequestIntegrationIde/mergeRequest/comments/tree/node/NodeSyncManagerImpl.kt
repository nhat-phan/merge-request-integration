package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.util.ui.tree.TreeUtil
import net.ntworld.mergeRequest.MergeRequestInfo
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import kotlin.math.min

class NodeSyncManagerImpl(
    private val nodeDescriptorService: NodeDescriptorService
) : NodeSyncManager {
    val myExpandingTreeNodeMap = mutableMapOf<String, MutableSet<String>>()
    val mySelectedTreeNodeMap = mutableMapOf<String, String>()

    override fun clear(mergeRequestInfo: MergeRequestInfo) {
        myExpandingTreeNodeMap.remove(mergeRequestInfo.id)
        mySelectedTreeNodeMap.remove(mergeRequestInfo.id)
    }

    override fun sync(mergeRequestInfo: MergeRequestInfo, root: RootNode, tree: NodeSyncManager.SyncedTree) {
        syncStructure(root, tree.treeRoot) { node, treeNode ->
            // TODO: do something when visit all nodes
            println("node ${node.id} is ${if(tree.isExpand(treeNode)) "expanding" else "collapsing"}")
        }
    }

    override fun makeSyncedTree(tree: JTree, treeRoot: DefaultMutableTreeNode): NodeSyncManager.SyncedTree {
        return MySyncedTree(tree, treeRoot)
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
                treeNode.add(childTreeNode)
                syncStructure(child, childTreeNode, visitor)
            }
            return
        }

        // TODO: add a test for case "index >= treeNodeChildCount" (resolved comment in line which has 1 thread)
        if (treeNodeChildCount > parent.childCount && index < treeNodeChildCount) {
            for (i in index until treeNodeChildCount) {
                treeNode.remove(i)
            }
            return
        }
    }

    private class MySyncedTree(
        private val tree: JTree,
        override val treeRoot: DefaultMutableTreeNode
    ) : NodeSyncManager.SyncedTree {
        override fun isExpand(treeNode: TreeNode) = tree.isExpanded(
            TreeUtil.getPath(treeRoot, treeNode)
        )
    }
}