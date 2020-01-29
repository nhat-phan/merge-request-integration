package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.query.GetMergeRequestFilter

interface MergeRequestApi {
    fun find(projectId: String, mergeRequestId: String): MergeRequest?

    fun approve(projectId: String, mergeRequestId: String, sha: String)

    fun unapprove(projectId: String, mergeRequestId: String)

    fun findApproval(projectId: String, mergeRequestId: String): Approval

    fun getPipelines(projectId: String, mergeRequestId: String): List<Pipeline>

    fun getCommits(projectId: String, mergeRequestId: String): List<Commit>

    fun search(
        projectId: String,
        currentUserId: String,
        filterBy: GetMergeRequestFilter,
        orderBy: MergeRequestOrdering,
        page: Int,
        itemsPerPage: Int
    ): SearchResult

    fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest

    interface SearchResult {
        val data: List<MergeRequestInfo>

        val totalPages: Int

        val totalItems: Int

        val currentPage: Int
    }
}
