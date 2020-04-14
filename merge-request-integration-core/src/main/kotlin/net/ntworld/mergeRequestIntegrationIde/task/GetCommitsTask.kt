package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.query.GetCommitsQuery
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContextManager
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class GetCommitsTask(
    private val applicationService: ApplicationService,
    ideaProject: Project,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val listener: Listener
) : Task.Backgroundable(ideaProject, "Fetching commit data...", false) {
    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    private class Indicator(private val task: GetCommitsTask) : BackgroundableProcessIndicator(task)

    override fun run(indicator: ProgressIndicator) {
        try {
            listener.taskStarted()
            val result = applicationService.infrastructure.queryBus() process GetCommitsQuery.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequestInfo.id
            )
            listener.dataReceived(mergeRequestInfo, result.commits)
            ReviewContextManager.updateCommits(providerData.id, mergeRequestInfo.id, result.commits)
            listener.taskEnded()
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    interface Listener {
        fun onError(exception: Exception) {}

        fun taskStarted() {}

        fun dataReceived(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>)

        fun taskEnded() {}
    }
}