package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.NodeFactory
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.RootNodeBuilder
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import javax.swing.JComponent
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.*

class CommentTreeViewImpl(
    private val projectService: ProjectService,
    private val providerData: ProviderData
) : AbstractView<CommentTreeView.ActionListener>(), CommentTreeView {
    override val dispatcher = EventDispatcher.create(CommentTreeView.ActionListener::class.java)

    private val myComponent = CustomSimpleToolWindowPanel(vertical = true, borderless = true)
    private val myToolbar = CommentTreeViewToolbar(dispatcher)

    private val myTree = Tree()
    private val myRoot = DefaultMutableTreeNode()
    private val myModel = DefaultTreeModel(myRoot)
    private val myRenderer = NodeRenderer()
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
        if (null !== it) {
            val lastPath = it.path.lastPathComponent as? DefaultMutableTreeNode ?: return@TreeSelectionListener
            val descriptor = lastPath.userObject as? PresentableNodeDescriptor<*> ?: return@TreeSelectionListener
            val element = descriptor.element as? Node ?: return@TreeSelectionListener
            dispatcher.multicaster.onTreeNodeSelected(element)
        }
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
        val builder = RootNodeBuilder(comments)

        NodeFactory.applyToTreeRoot(projectService, providerData, builder.build(), myRoot)
        myModel.nodeStructureChanged(myRoot)
    }

    override fun setShowResolvedCommentState(selected: Boolean) {
        myToolbar.showResolved = selected
    }

    override val component: JComponent = myComponent
}