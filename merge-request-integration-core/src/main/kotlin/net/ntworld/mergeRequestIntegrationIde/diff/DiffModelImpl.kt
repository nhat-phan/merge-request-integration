package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractModel
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import java.util.*

class DiffModelImpl(
    override val providerData: ProviderData?,
    override val mergeRequest: MergeRequest?,
    override val commits: List<Commit>,
    override val change: Change,
    override val commentsOnBeforeSide: List<CommentPoint>,
    override val commentsOnAfterSide: List<CommentPoint>
) : AbstractModel<EventListener>(), DiffModel {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)
}