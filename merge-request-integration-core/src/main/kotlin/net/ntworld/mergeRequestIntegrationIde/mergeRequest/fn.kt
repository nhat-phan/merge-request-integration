package net.ntworld.mergeRequestIntegrationIde.mergeRequest

import net.ntworld.mergeRequest.DateTime
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState

private class MergeRequestInfoEmpty : MergeRequestInfo {
    override val id: String = ""
    override val provider: String = ""
    override val projectId: String = ""
    override val title: String = ""
    override val description: String = ""
    override val url: String = ""
    override val state: MergeRequestState = MergeRequestState.CLOSED
    override val createdAt: DateTime = ""
    override val updatedAt: DateTime = ""
}

fun MergeRequestInfo.isEmpty(): Boolean {
    return this.id.isEmpty()
}

val MergeRequestInfo.Companion.Empty: MergeRequestInfo
    get() = MergeRequestInfoEmpty()
