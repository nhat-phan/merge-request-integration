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

    val displayResolvedComments: Boolean

    fun isChangesBuilt(): Boolean

    fun isFetchedComments(): Boolean

    fun shutdown()

    fun openChange(change: Change)

    fun findChangeByPath(absolutePath: String): Change?

    fun findCommentsByPath(absolutePath: String): List<Comment>

    fun fetchComments()

    fun key(): String {
        return keyOf(providerData, branchName)
    }

    companion object {
        fun keyOf(providerData: ProviderData, branchName: String): String {
            return "${providerData.id}:${branchName}"
        }
    }
}