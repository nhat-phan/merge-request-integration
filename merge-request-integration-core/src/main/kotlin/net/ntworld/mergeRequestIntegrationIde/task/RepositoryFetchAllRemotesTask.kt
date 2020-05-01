package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import git4idea.fetch.GitFetchSupport
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil

class RepositoryFetchAllRemotesTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData
) : Task.Backgroundable(projectServiceProvider.project, "Fetching...", true) {
    override fun run(indicator: ProgressIndicator) {
        val repository = RepositoryUtil.findRepository(projectServiceProvider, providerData)
        if (null !== repository) {
            val service = ServiceManager.getService(projectServiceProvider.project, GitFetchSupport::class.java)
            service.fetchAllRemotes(listOf(repository))
        }
    }
}