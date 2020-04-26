package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequest.UserStatus
import net.ntworld.mergeRequest.query.GetProjectMembersQuery
import net.ntworld.mergeRequestIntegration.internal.UserInfoImpl
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import com.intellij.openapi.project.Project as IdeaProject

class FetchProjectMembersTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val addEmptyMember: Boolean,
    private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching project members...", false) {
    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    override fun run(indicator: ProgressIndicator) {
        try {
            listener.taskStarted()
            val query = GetProjectMembersQuery.make(providerData.id)
            val result = projectServiceProvider.infrastructure.queryBus() process query
            val data = result.members
                .filter { it.status == UserStatus.ACTIVE }
                .sortedBy { it.name }
            if (addEmptyMember) {
                val dataWithEmptyMember = mutableListOf<UserInfo>(UserInfoImpl.None)
                listener.dataReceived(dataWithEmptyMember + data)
            } else {
                listener.dataReceived(data)
            }
            listener.taskEnded()
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    private class Indicator(private val task: FetchProjectMembersTask) : BackgroundableProcessIndicator(task)

    interface Listener {
        fun onError(exception: Exception)

        fun taskStarted()

        fun dataReceived(collection: List<UserInfo>)

        fun taskEnded()
    }
}