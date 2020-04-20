package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import net.ntworld.mergeRequest.MergeRequestInfo
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

interface NodeSyncManager {
    fun sync(mergeRequestInfo: MergeRequestInfo, root: RootNode, tree: SyncedTree)

    fun makeSyncedTree(tree: JTree, treeModel: DefaultTreeModel, treeRoot: DefaultMutableTreeNode): SyncedTree
}