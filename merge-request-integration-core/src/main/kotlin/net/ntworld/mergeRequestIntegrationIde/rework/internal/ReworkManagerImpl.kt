package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vcs.changes.Change
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.debug
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.rework.BranchWatcher
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkManager
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import net.ntworld.mergeRequestIntegrationIde.task.SearchMergeRequestTask
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import java.util.*

internal class ReworkManagerImpl(
    private val projectServiceProvider: ProjectServiceProvider
) : ReworkManager {
    private val myBranchWatchers = Collections.synchronizedMap(mutableMapOf<String, BranchWatcher>())
    private val myReworkWatchers = Collections.synchronizedMap(mutableMapOf<String, ReworkWatcher>())

    override fun clear() {
        myBranchWatchers.forEach { entry -> entry.value.shutdown() }
        myBranchWatchers.clear()

        myReworkWatchers.forEach { entry -> entry.value.shutdown() }
        myReworkWatchers.clear()
    }

    override fun markReworkWatcherTerminated(reworkWatcher: ReworkWatcher) {
        val key = keyOf(reworkWatcher.providerData, reworkWatcher.branchName)
        myReworkWatchers.remove(key)

        debug("clear ReworkManager $key")
    }

    override fun createBranchWatcher(providerData: ProviderData) {
        if (providerData.status != ProviderStatus.ACTIVE || myBranchWatchers.containsKey(providerData.id)) {
            return
        }

        var gitRepository = RepositoryUtil.findRepository(projectServiceProvider, providerData)
        var count = 0
        while (gitRepository === null && count < 100) {
            debug("Cannot find repository for ${providerData.id}, retry in 10s")
            count++
            Thread.sleep(10000)
            gitRepository = RepositoryUtil.findRepository(projectServiceProvider, providerData)
        }

        val repository = gitRepository
        if (null !== repository) {
            val branchWatcher = BranchWatcherImpl(
                this, providerData, repository
            )
            myBranchWatchers[providerData.id] = branchWatcher

            debug("Create BranchWatcher for ${providerData.id}")
            projectServiceProvider.applicationServiceProvider.watcherManager.addWatcher(branchWatcher)
        }
    }

    override fun requestCreateReworkWatcher(providers: List<ProviderData>, branchName: String) {
        val pair = findProviderData(providers, branchName)
        val providerData = pair.first
        val repository = pair.second
        if (null !== providerData && null !== repository) {
            requestCreateReworkWatcher(providerData, repository, branchName)
        }
    }

    override fun requestCreateReworkWatcher(providerData: ProviderData, repository: GitRepository, branchName: String) {
        val key = keyOf(providerData, branchName)
        if (myReworkWatchers.contains(key)) {
            return
        }

        val task = SearchMergeRequestTask(
            projectServiceProvider,
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
                    if (myReworkWatchers.contains(key)) {
                        return
                    }

                    ApplicationManager.getApplication().invokeLater {
                        if (list.isNotEmpty()) {
                            val mergeRequestInfo = list.first()
                            debug("Create watcher for ${providerData.id}:${mergeRequestInfo.id}")
                            val reworkWatcher = ReworkWatcherImpl(
                                projectServiceProvider,
                                repository,
                                branchName,
                                providerData,
                                mergeRequestInfo
                            )
                            myReworkWatchers[key] = reworkWatcher
                            projectServiceProvider.applicationServiceProvider.watcherManager.addWatcher(reworkWatcher)
                        }
                    }
                }
            }
        )

        task.start()
    }

    override fun findReworkWatcherByChange(providerData: ProviderData, change: Change): ReworkWatcher? {
        for (entry in myReworkWatchers) {
            if (entry.value.providerData.id != providerData.id) {
                continue
            }

            if (entry.value.changes.contains(change)) {
                return entry.value
            }
        }
        return null
    }

    private fun keyOf(providerData: ProviderData, branchName: String): String {
        return "${providerData.id}:${branchName}"
    }

    private fun findProviderData(
        providers: List<ProviderData>,
        branchName: String
    ): Pair<ProviderData?, GitRepository?> {
        for (provider in providers) {
            val repository = RepositoryUtil.findRepository(projectServiceProvider, provider)
            if (null === repository) {
                continue
            }

            if (repository.currentBranchName != branchName) {
                continue
            }

            return Pair(provider, repository)
        }
        return Pair(null, null)
    }
}