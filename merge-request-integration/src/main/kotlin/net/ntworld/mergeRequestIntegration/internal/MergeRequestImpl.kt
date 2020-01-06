package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.*

class MergeRequestImpl(
    override val id: String,
    override val provider: String,
    override val projectId: String,
    override val title: String,
    override val description: String,
    override val url: String,
    override val state: MergeRequestState,
    override val createdAt: DateTime,
    override val updatedAt: DateTime,
    override val assignee: UserInfo?,
    override val author: UserInfo,
    override val diffReference: DiffReference?,
    override val sourceBranch: String,
    override val targetBranch: String,
    override val upVotes: Int,
    override val downVotes: Int,
    override val commentsCount: Int,
    override val isWorkInProgress: Boolean,
    override val canMerged: Boolean,
    override val mergedBy: UserInfo?,
    override val closedBy: UserInfo?,
    override val mergedAt: DateTime?,
    override val closedAt: DateTime?
) : MergeRequest