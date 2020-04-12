package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface CommentTreeModel : Model<CommentTreeModel.DataListener> {
    var comments: List<Comment>

    var displayResolvedComments: Boolean

    interface DataListener : EventListener {
        fun onCommentsUpdated()

        fun onDisplayResolvedCommentsChanged()
    }
}