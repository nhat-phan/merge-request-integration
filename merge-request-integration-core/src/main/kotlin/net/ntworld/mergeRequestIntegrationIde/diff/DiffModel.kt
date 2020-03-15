package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.SimpleModel
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint

interface DiffModel : SimpleModel {
    val providerData: ProviderData?

    val mergeRequest: MergeRequest?

    val change: Change

    val commentsOnBeforeSide: List<CommentPoint>

    val commentsOnAfterSide: List<CommentPoint>
}
