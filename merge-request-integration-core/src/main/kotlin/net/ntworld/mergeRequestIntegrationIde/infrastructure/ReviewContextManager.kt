package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.watcher.Watcher

interface ReviewContextManager : Watcher {
    fun initContext(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, selected: Boolean)

    fun isDoingCodeReview(providerId: String, mergeRequestId: String): Boolean

    fun findDoingCodeReviewContext(): ReviewContext?

    fun findSelectedContext(): ReviewContext?

    fun findContext(providerId: String, mergeRequestId: String): ReviewContext?

    fun setSelected(providerId: String, mergeRequestId: String)

    fun clearContextDoingCodeReview()

    fun setContextToDoingCodeReview(providerId: String, mergeRequestId: String)

    fun updateComments(providerId: String, mergeRequestId: String, comments: List<Comment>)

    fun updateCommits(providerId: String, mergeRequestId: String, commits: List<Commit>)

    fun updateChanges(providerId: String, mergeRequestId: String, changes: List<Change>)

    fun updateReviewingCommits(providerId: String, mergeRequestId: String, commits: List<Commit>)

    fun updateReviewingChanges(providerId: String, mergeRequestId: String, changes: List<Change>)

    fun updateMergeRequest(providerId: String, mergeRequest: MergeRequest)
}
