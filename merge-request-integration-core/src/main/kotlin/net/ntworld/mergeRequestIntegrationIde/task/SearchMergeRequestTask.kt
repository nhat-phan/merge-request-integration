package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequest.query.GetMergeRequestsQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.SEARCH_MERGE_REQUEST_ITEMS_PER_PAGE
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

class SearchMergeRequestTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val filtering: GetMergeRequestFilter,
    private val ordering: MergeRequestOrdering,
    private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching merge requests...", false) {
    var page: Int = 1
    fun start() = start(1)

    fun start(page: Int) {
        this.page = page
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    override fun run(indicator: ProgressIndicator) {
        try {
            val currentPage = page
            listener.taskStarted()
            val query = GetMergeRequestsQuery.make(
                providerId = providerData.id,
                filterBy = filtering,
                orderBy = ordering,
                page = currentPage,
                itemsPerPage = SEARCH_MERGE_REQUEST_ITEMS_PER_PAGE
            )
            val result = projectServiceProvider.infrastructure.queryBus() process query
            listener.dataReceived(result.mergeRequests, currentPage, result.totalPages, result.totalItems)
            listener.taskEnded()
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    private class Indicator(private val task: SearchMergeRequestTask) : BackgroundableProcessIndicator(task)

    interface Listener {
        fun onError(exception: Exception) {
            throw exception
        }

        fun taskStarted() {}

        fun dataReceived(list: List<MergeRequestInfo>, page: Int, totalPages: Int, totalItems: Int)

        fun taskEnded() {}
    }
}