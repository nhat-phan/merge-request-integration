package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.api.MergeRequestApi

data class MergeRequestSearchResultImpl(
    override val data: List<MergeRequestInfo>,
    override val totalPages: Int,
    override val totalItems: Int,
    override val currentPage: Int
): MergeRequestApi.SearchResult