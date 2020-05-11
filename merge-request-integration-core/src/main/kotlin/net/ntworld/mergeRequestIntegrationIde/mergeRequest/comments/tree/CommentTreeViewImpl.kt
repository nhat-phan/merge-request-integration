package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.tree.TreeUtil
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.*
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import javax.swing.JComponent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.*

class CommentTreeViewImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData
) : AbstractView<CommentTreeView.ActionListener>(), CommentTreeView {
    override val dispatcher = EventDispatcher.create(CommentTreeView.ActionListener::class.java)

    private val myComponent = CustomSimpleToolWindowPanel(vertical = true, borderless = true)

    private val myTree = Tree()
    private val myRoot = DefaultMutableTreeNode()
    private val myModel = DefaultTreeModel(myRoot)
    private val myRenderer = NodeRenderer()
    private var myIsTreeRendering = false
    private val myTreeCellRenderer = TreeCellRenderer { tree, value, selected, expanded, leaf, row, hasFocus ->
        myRenderer.getTreeCellRendererComponent(
            tree,
            value,
            selected,
            expanded,
            leaf,
            row,
            hasFocus
        )
    }
    private val myTreeSelectionListener = TreeSelectionListener {
        if (null !== it && !myIsTreeRendering) {
            handleOnTreeNodeSelectedEvent(it.path)
        }
    }

    private val myToolbar = CommentTreeViewToolbar(myTree, dispatcher)
    private val nodeSyncManager: NodeSyncManager by lazy {
        NodeSyncManagerImpl(NodeDescriptorServiceImpl(projectServiceProvider, providerData))
    }
    private val mySyncedTree: SyncedTree by lazy {
        nodeSyncManager.makeSyncedTree(myTree, myModel, myRoot)
    }

    init {
        val treeSelectionModel = DefaultTreeSelectionModel()
        treeSelectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        myTree.model = myModel
        myTree.cellRenderer = myTreeCellRenderer
        myTree.isRootVisible = false
        myTree.selectionModel = treeSelectionModel

        myTree.addTreeSelectionListener(myTreeSelectionListener)

        myComponent.setContent(ScrollPaneFactory.createScrollPane(myTree, true))
        myComponent.toolbar = myToolbar.component
    }

    override fun renderTree(mergeRequestInfo: MergeRequestInfo, comments: List<Comment>) {
        myIsTreeRendering = true
        val builder = RootNodeBuilder(comments)
        val root = builder.build()
        nodeSyncManager.sync(mergeRequestInfo, root, mySyncedTree)
        handleOnTreeNodeSelectedEvent(myTree.selectionPath)

        myIsTreeRendering = false
    }

    override fun setShowResolvedCommentState(selected: Boolean) {
        myToolbar.showResolved = selected
    }

    override fun hasGeneralCommentsTreeNode(): Boolean {
        val children = myRoot.children()
        for (child in children) {
            if (isGeneralCommentsTreeNode(child)) {
                return true
            }
        }
        return false
    }

    override fun selectGeneralCommentsTreeNode() {
        val children = myRoot.children()
        for (child in children) {
            if (isGeneralCommentsTreeNode(child)) {
                myTree.selectionPath = TreeUtil.getPath(myRoot, child)
                break
            }
        }
    }

    override fun setToolbarMode(mode: CommentTreeView.ToolbarMode) {
        myToolbar.setMode(mode)
    }

    private fun isGeneralCommentsTreeNode(node: TreeNode) : Boolean {
        val treeNode = node as? DefaultMutableTreeNode ?: return false
        val descriptor = treeNode.userObject as? PresentableNodeDescriptor<*> ?: return false
        return descriptor.element is GeneralCommentsNode
    }

    private fun handleOnTreeNodeSelectedEvent(selectedPath: TreePath?) {
        if (null === selectedPath) {
            return
        }

        val lastPath = selectedPath.lastPathComponent as? DefaultMutableTreeNode ?: return
        val descriptor = lastPath.userObject as? PresentableNodeDescriptor<*> ?: return
        val element = descriptor.element as? Node ?: return
        dispatcher.multicaster.onTreeNodeSelected(element)
    }

    override val component: JComponent = myComponent
}