package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequestIntegrationIde.Component
import net.ntworld.mergeRequestIntegrationIde.Presenter
import javax.swing.JComponent

interface CommentTreePresenter : Presenter<CommentTreePresenter.Listener>, Component {
    val model: CommentTreeModel

    val view: CommentTreeView

    override val component: JComponent
        get() = view.component

    fun setToolbarMode(mode: CommentTreeView.ToolbarMode) = view.setToolbarMode(mode)

    fun hasGeneralCommentsTreeNode(): Boolean

    fun selectGeneralCommentsTreeNode()

    interface Listener : CommentTreeView.ActionListener
}