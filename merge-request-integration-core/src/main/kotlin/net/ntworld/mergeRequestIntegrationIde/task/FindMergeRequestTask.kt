package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.query.FindMergeRequestQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

class FindMergeRequestTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching merge request...", false) {
    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    private class Indicator(private val task: FindMergeRequestTask) : BackgroundableProcessIndicator(task)

    override fun run(indicator: ProgressIndicator) {
        try {
            listener.taskStarted()
            val result = projectServiceProvider.infrastructure.queryBus() process FindMergeRequestQuery.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequestInfo.id
            )
            listener.dataReceived(result.mergeRequest)
            projectServiceProvider.reviewContextManager.updateMergeRequest(providerData.id, result.mergeRequest)
            listener.taskEnded()
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    interface Listener {
        fun onError(exception: Exception) {
            throw exception
        }

        fun taskStarted() {}

        fun dataReceived(mergeRequest: MergeRequest)

        fun taskEnded() {}
    }

}