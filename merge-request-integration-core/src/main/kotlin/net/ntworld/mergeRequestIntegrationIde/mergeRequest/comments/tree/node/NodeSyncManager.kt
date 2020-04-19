package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.MergeRequestInfo
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

interface NodeSyncManager {
    fun clear(mergeRequestInfo: MergeRequestInfo)

    fun sync(mergeRequestInfo: MergeRequestInfo, root: RootNode, tree: SyncedTree)

    fun makeSyncedTree(tree: JTree, treeRoot: DefaultMutableTreeNode): SyncedTree

    interface SyncedTree {
        val treeRoot: DefaultMutableTreeNode

        fun isExpand(treeNode: TreeNode): Boolean
    }
}