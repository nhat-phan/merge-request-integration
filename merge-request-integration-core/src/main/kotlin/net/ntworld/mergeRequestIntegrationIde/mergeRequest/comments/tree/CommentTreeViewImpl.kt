package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import javax.swing.JComponent
import javax.swing.tree.*

class CommentTreeViewImpl : AbstractView<CommentTreeView.ActionListener>(), CommentTreeView {
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

    init {
        val treeSelectionModel = DefaultTreeSelectionModel()
        treeSelectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        myTree.model = myModel
        myTree.cellRenderer = myTreeCellRenderer
        myTree.isRootVisible = false
        myTree.selectionModel = treeSelectionModel

        myComponent.setContent(ScrollPaneFactory.createScrollPane(myTree, true))
        myComponent.toolbar = myToolbar.component
    }

    override fun renderTree(comments: List<Comment>) {
//        val builder = TreeNodeBuilder(comments)
//
//        TreeNodeFactory.applyToTreeRoot(ideaProject, builder.build(), myRoot)
//        myModel.nodeStructureChanged(myRoot)
    }

    override fun setShowResolvedCommentState(selected: Boolean) {
        myToolbar.showResolved = selected
    }

    override val component: JComponent = myComponent
}