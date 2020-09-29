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
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.*

class CommentTreeViewImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val showOpenDiffViewDescription: Boolean
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
            handleOnTreeNodeSelectedEvent(it.path, CommentTreeView.TreeSelectType.NORMAL)
        }
    }
    private val myTreeMouseListener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
            if (null === e) {
                return
            }

            if (e.clickCount == 2) {
                handleOnTreeNodeSelectedEvent(myTree.selectionPath, CommentTreeView.TreeSelectType.DOUBLE_CLICK)
            }
        }
    }
    private val myKeyListener = object: KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (null === e) {
                return
            }
            if (e.keyCode == KeyEvent.VK_ENTER) {
                handleOnTreeNodeSelectedEvent(myTree.selectionPath, CommentTreeView.TreeSelectType.PRESS_ENTER)
            }
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
        myTree.addMouseListener(myTreeMouseListener)
        myTree.addKeyListener(myKeyListener)

        myComponent.setContent(ScrollPaneFactory.createScrollPane(myTree, true))
        myComponent.toolbar = myToolbar.component
    }

    override fun renderTree(mergeRequestInfo: MergeRequestInfo, comments: List<Comment>) {
        myIsTreeRendering = true
        val builder = RootNodeBuilder(comments, showOpenDiffViewDescription)
        val root = builder.build()
        nodeSyncManager.sync(mergeRequestInfo, root, mySyncedTree)
        handleOnTreeNodeSelectedEvent(myTree.selectionPath, CommentTreeView.TreeSelectType.NORMAL)

        myIsTreeRendering = false
    }

    override fun setShowResolvedCommentState(selected: Boolean) {
        myToolbar.showResolved = selected
    }

    override fun hasGeneralCommentsTreeNode(): Boolean {
        val children = myRoot.children()
        for (child in children) {
            if (isGeneralCommentsTreeNode(child as TreeNode)) {
                return true
            }
        }
        return false
    }

    override fun selectGeneralCommentsTreeNode() {
        val children = myRoot.children()
        for (child in children) {
            if (isGeneralCommentsTreeNode(child as TreeNode)) {
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

    private fun handleOnTreeNodeSelectedEvent(selectedPath: TreePath?, type: CommentTreeView.TreeSelectType) {
        if (null === selectedPath) {
            return
        }

        val lastPath = selectedPath.lastPathComponent as? DefaultMutableTreeNode ?: return
        val descriptor = lastPath.userObject as? PresentableNodeDescriptor<*> ?: return
        val element = descriptor.element as? Node ?: return
        dispatcher.multicaster.onTreeNodeSelected(element, type)
    }

    override val component: JComponent = myComponent
}