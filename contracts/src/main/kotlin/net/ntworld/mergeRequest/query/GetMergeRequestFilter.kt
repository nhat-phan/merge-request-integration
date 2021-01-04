package net.ntworld.mergeRequest.query

import net.ntworld.mergeRequest.MergeRequestState

interface GetMergeRequestFilter {
    val id: Int?

    val state: MergeRequestState

    val search: String

    val authorId: String

    val assigneeId: String

    val approverIds: List<String>

    val sourceBranch: String

    companion object
}