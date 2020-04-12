package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequestIntegrationIde.Component
import net.ntworld.mergeRequestIntegrationIde.SimplePresenter
import javax.swing.JComponent

interface CommentTreePresenter : SimplePresenter, Component {
    val model: CommentTreeModel

    val view: CommentTreeView

    override val component: JComponent
        get() = view.component
}