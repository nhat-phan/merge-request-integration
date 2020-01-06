package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.DateTime
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState

data class MergeRequestInfoImpl(
    override val id: String,
    override val provider: String,
    override val projectId: String,
    override val title: String,
    override val description: String,
    override val url: String,
    override val state: MergeRequestState,
    override val createdAt: DateTime,
    override val updatedAt: DateTime
) : MergeRequestInfo