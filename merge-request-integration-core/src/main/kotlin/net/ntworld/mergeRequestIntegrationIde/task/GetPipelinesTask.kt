package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.Pipeline
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.query.GetPipelinesQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

class GetPipelinesTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching pipeline data...", true) {
    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    private class Indicator(private val task: GetPipelinesTask) : BackgroundableProcessIndicator(task)

    override fun run(indicator: ProgressIndicator) {
        try {
            listener.taskStarted()
            val result = projectServiceProvider.infrastructure.queryBus() process GetPipelinesQuery.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequestInfo.id
            )
            listener.dataReceived(mergeRequestInfo, result.pipelines)
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

        fun dataReceived(mergeRequestInfo: MergeRequestInfo, pipelines: List<Pipeline>)

        fun taskEnded() {}
    }
}