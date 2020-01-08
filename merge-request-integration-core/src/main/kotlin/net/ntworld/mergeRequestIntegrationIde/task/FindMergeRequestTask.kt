package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.MergeRequest
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.query.FindMergeRequestQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class FindMergeRequestTask(
    ideaProject: IdeaProject,
    private val providerData: ProviderData,
    private val mergeRequestId: String,
    private val listener: Listener
) : Task.Backgroundable(ideaProject, "Fetching merge request...", false) {
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
            val result = ApplicationService.instance.infrastructure.queryBus() process FindMergeRequestQuery.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequestId
            )
            listener.dataReceived(result.mergeRequest)
            listener.taskEnded()
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    interface Listener {
        fun onError(exception: Exception) {}

        fun taskStarted() {}

        fun dataReceived(mergeRequest: MergeRequest)

        fun taskEnded() {}
    }

}