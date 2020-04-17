package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.Disposable
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.MessageBusConnection
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.Model
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import java.util.*

interface DiffModel : Model<DiffModel.DataListener>, Disposable {
    val reviewContext: ReviewContext

    val providerData: ProviderData

    val mergeRequestInfo: MergeRequestInfo

    val messageBusConnection: MessageBusConnection

    val diffReference: DiffReference?

    val commits: List<Commit>

    val change: Change

    val commentsOnBeforeSide: List<CommentPoint>

    val commentsOnAfterSide: List<CommentPoint>

    var displayResolvedComments: Boolean

    fun rebuildComments(showResolved: Boolean)

    interface DataListener : EventListener {
        fun onCommentsUpdated(source: DataChangedSource)
    }
}
