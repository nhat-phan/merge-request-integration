package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface CommentTreeModel : Model<CommentTreeModel.DataListener> {
    val providerData: ProviderData

    var mergeRequestInfo: MergeRequestInfo

    var comments: List<Comment>

    var displayResolvedComments: Boolean

    interface DataListener : EventListener {
        fun onMergeRequestInfoChanged()

        fun onCommentsUpdated()

        fun onDisplayResolvedCommentsChanged()
    }
}