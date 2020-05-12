package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.query.FindApprovalQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

class FindApprovalTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching approval data...", false) {
    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    private class Indicator(private val task: FindApprovalTask) : BackgroundableProcessIndicator(task)

    override fun run(indicator: ProgressIndicator) {
        try {
            listener.taskStarted()
            val result = projectServiceProvider.infrastructure.queryBus() process FindApprovalQuery.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequestInfo.id
            )
            listener.dataReceived(mergeRequestInfo, result.approval)
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

        fun dataReceived(mergeRequestInfo: MergeRequestInfo, approval: Approval)

        fun taskEnded() {}
    }
}