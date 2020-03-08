package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import java.util.*

class DiffModelImpl(
    override val mergeRequest: MergeRequest?,
    override val change: Change,
    override val commentsOnBeforeSide: List<CommentPoint>,
    override val commentsOnAfterSide: List<CommentPoint>
) : DiffModel {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

}