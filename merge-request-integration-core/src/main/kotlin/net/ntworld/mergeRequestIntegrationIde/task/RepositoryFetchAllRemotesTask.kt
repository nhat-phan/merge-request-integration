package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import git4idea.fetch.GitFetchSupport
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import com.intellij.openapi.project.Project as IdeaProject

class RepositoryFetchAllRemotesTask(
    private val ideaProject: IdeaProject,
    private val providerData: ProviderData
) : Task.Backgroundable(ideaProject, "Fetching...", true) {
    override fun run(indicator: ProgressIndicator) {
        val repository = RepositoryUtil.findRepository(ideaProject, providerData)
        if (null !== repository) {
            val service = ServiceManager.getService(ideaProject, GitFetchSupport::class.java)
            service.fetchAllRemotes(listOf(repository))
        }
    }
}