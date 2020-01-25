package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.DateTime
import net.ntworld.mergeRequest.Pipeline
import net.ntworld.mergeRequest.PipelineStatus

data class PipelineImpl(
    override val id: String,
    override val hash: String,
    override val ref: String,
    override val status: PipelineStatus,
    override val url: String,
    override val createdAt: DateTime?,
    override val updatedAt: DateTime?
) : Pipeline