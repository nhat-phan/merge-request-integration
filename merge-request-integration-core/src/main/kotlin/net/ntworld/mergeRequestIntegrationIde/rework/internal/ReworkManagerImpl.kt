package net.ntworld.mergeRequestIntegrationIde.rework.internal

import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.rework.BranchWatcher
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkManager
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import net.ntworld.mergeRequestIntegrationIde.task.SearchMergeRequestTask
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import java.util.*

internal class ReworkManagerImpl(
    private val serviceProvider: ProjectServiceProvider
) : ReworkManager {
    private val myBranchWatchers = Collections.synchronizedMap(mutableMapOf<String, BranchWatcher>())
    private val myReworkWatchers = Collections.synchronizedMap(mutableMapOf<String, ReworkWatcher>())

    override fun clearAllBranchWatchers() {
        myBranchWatchers.forEach { entry -> entry.value.shutdown() }
        myBranchWatchers.clear()
    }

    override fun createBranchWatcher(providerData: ProviderData) {
        val repository = RepositoryUtil.findRepository(serviceProvider.project, providerData)
        if (null === repository) {
            return
        }

        val branchWatcher = BranchWatcherImpl(
            this, providerData, repository
        )
        myBranchWatchers[providerData.id] = branchWatcher
        serviceProvider.applicationServiceProvider.watcherManager.addWatcher(branchWatcher)
    }

    override fun requestCreateReworkWatcher(providers: List<ProviderData>, branchName: String) {
        val providerData = findProviderData(providers, branchName)
        if (null !== providerData) {
            requestCreateReworkWatcher(providerData, branchName)
        }
    }

    override fun requestCreateReworkWatcher(providerData: ProviderData, branchName: String) {
        val task = SearchMergeRequestTask(
            serviceProvider,
            providerData,
            GetMergeRequestFilter.make(
                state = MergeRequestState.OPENED,
                search = "",
                authorId = "",
                assigneeId = "",
                approverIds = listOf(),
                sourceBranch = branchName
            ),
            MergeRequestOrdering.RECENTLY_UPDATED,
            object : SearchMergeRequestTask.Listener {
                override fun dataReceived(list: List<MergeRequestInfo>, page: Int, totalPages: Int, totalItems: Int) {
                    if (list.isNotEmpty()) {
                        val mergeRequestInfo = list.first()
                        println("Create watcher for $mergeRequestInfo")
                    }
                }
            })

        task.start()
    }

    private fun findProviderData(providers: List<ProviderData>, branchName: String): ProviderData? {
        for (provider in providers) {
            val repository = RepositoryUtil.findRepository(serviceProvider.project, provider)
            if (null === repository) {
                continue
            }

            if (repository.currentBranchName != branchName) {
                continue
            }

            return provider
        }
        return null
    }
}