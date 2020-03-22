package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.query.GetCommentsQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class GetCommentsTask(
    private val applicationService: ApplicationService,
    ideaProject: Project,
    private val providerData: ProviderData,
    private val mergeRequest: MergeRequest,
    private val listener: Listener
) : Task.Backgroundable(ideaProject, "Fetching comment data...", false) {
    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    private class Indicator(private val task: GetCommentsTask) : BackgroundableProcessIndicator(task)

    override fun run(indicator: ProgressIndicator) {
        try {
            listener.taskStarted()
            val result = applicationService.infrastructure.queryBus() process GetCommentsQuery.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequest.id
            )
            listener.dataReceived(providerData, mergeRequest, result.comments)
            listener.taskEnded()
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    interface Listener {
        fun onError(exception: Exception) {}

        fun taskStarted() {}

        fun dataReceived(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>)

        fun taskEnded() {}
    }
}