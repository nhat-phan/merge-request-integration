package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequestIntegrationIde.Component
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node
import java.util.*

interface CommentTreeView : View<CommentTreeView.ActionListener>, Component {

    fun renderTree(mergeRequestInfo: MergeRequestInfo, comments: List<Comment>)

    fun setShowResolvedCommentState(selected: Boolean)

    fun hasGeneralCommentsTreeNode(): Boolean

    fun selectGeneralCommentsTreeNode()

    fun setToolbarMode(mode: ToolbarMode)

    interface ActionListener : EventListener {
        fun onTreeNodeSelected(node: Node, type: TreeSelectType)

        fun onShowResolvedCommentsToggled(displayResolvedComments: Boolean)

        fun onCreateGeneralCommentClicked()

        fun onRefreshButtonClicked()
    }

    enum class ToolbarMode {
        FULL,
        MINI
    }

    enum class TreeSelectType {
        NORMAL,
        DOUBLE_CLICK,
        PRESS_ENTER
    }
}