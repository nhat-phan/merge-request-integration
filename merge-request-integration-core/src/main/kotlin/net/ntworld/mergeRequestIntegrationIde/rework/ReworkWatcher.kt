package net.ntworld.mergeRequestIntegrationIde.rework

import com.intellij.openapi.vcs.changes.Change
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.watcher.Watcher

interface ReworkWatcher : Watcher {

    val projectServiceProvider: ProjectServiceProvider

    val repository: GitRepository

    val branchName: String

    val providerData: ProviderData

    val mergeRequestInfo: MergeRequestInfo

    val commits: List<Commit>

    val changes: List<Change>

    val comments: List<Comment>

    fun shutdown()
}