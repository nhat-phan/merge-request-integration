package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.MessageBusConnection
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*

interface CodeReviewManager : CodeReviewUtil, Disposable {
    val providerData: ProviderData
    val mergeRequest: MergeRequest
    val messageBusConnection: MessageBusConnection
    val repository: GitRepository?

    var commits: Collection<Commit>
    var changes: Collection<Change>
    var comments: Collection<Comment>
}