package net.ntworld.mergeRequestIntegrationIde.ui.service

import git4idea.GitVcs
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.task.RepositoryFetchAllRemotesTask
import java.util.*
import com.intellij.openapi.project.Project as IdeaProject

object FetchService {
    private var myLastFetchedDateTime: Date? = null

    fun start(applicationServiceProvider: ApplicationServiceProvider, ideaProject: IdeaProject, providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
        val lastFetch = myLastFetchedDateTime
        val lastUpdated = DateTimeUtil.toDate(mergeRequestInfo.updatedAt)
        if (lastFetch !== null && lastFetch > lastUpdated) {
            return
        }

        try {
            GitVcs.runInBackground(RepositoryFetchAllRemotesTask(ideaProject, providerData))
        } catch (exception: Exception) {
            applicationServiceProvider.findProjectServiceProvider(ideaProject).notify(
                "Cannot fetch from remotes. The changes in Commits tab may not be displayed correctly. \n Please run 'git fetch' manually if you want to see the change list."
            )
        }
        myLastFetchedDateTime = Date(System.currentTimeMillis())
    }

}