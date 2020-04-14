package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.Disposable
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface DiffModel : Model<DiffModel.DataListener>, Disposable {
    val providerData: ProviderData

    val mergeRequestInfo: MergeRequestInfo

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
