package net.ntworld.mergeRequestIntegrationIde.comments.model

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.MergeRequest

data class DiffCommentsModel(
    val mergeRequest: MergeRequest,
    val change: Change
)