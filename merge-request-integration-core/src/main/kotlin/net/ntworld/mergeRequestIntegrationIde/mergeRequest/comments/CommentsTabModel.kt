package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface CommentsTabModel : Model<CommentsTabModel.DataListener>, Disposable {
    val providerData: ProviderData

    var mergeRequestInfo: MergeRequestInfo

    val comments: List<Comment>

    var displayResolvedComments: Boolean

    interface DataListener : EventListener {
        fun onMergeRequestInfoChanged()

        fun onCommentsUpdated(source: DataChangedSource)
    }
}