package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.Commit

data class CommitImpl(
    override val id: String,
    override val message: String,
    override val authorName: String,
    override val authorEmail: String,
    override val createdAt: String
) : Commit