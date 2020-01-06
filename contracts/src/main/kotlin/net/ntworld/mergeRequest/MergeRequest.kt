package net.ntworld.mergeRequest

interface MergeRequest: MergeRequestInfo {
    val assignee: UserInfo?

    val author: UserInfo

    val diffReference: DiffReference?

    val sourceBranch: String

    val targetBranch: String

    val upVotes: Int

    val downVotes: Int

    val commentsCount: Int

    val isWorkInProgress: Boolean

    val canMerged: Boolean

    val mergedBy: UserInfo?

    val closedBy: UserInfo?

    val mergedAt: DateTime?

    val closedAt: DateTime?

    companion object
}
