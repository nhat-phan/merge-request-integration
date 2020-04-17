package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.MessageBusConnection
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil

internal class CodeReviewManagerImpl(
    private val ideaProject: IdeaProject,
    override val providerData: ProviderData,
    override val mergeRequest: MergeRequest,
    val util: CodeReviewUtil
) : CodeReviewManager, CodeReviewUtil by util {
    override val repository: GitRepository? = RepositoryUtil.findRepository(ideaProject, providerData)
    override val messageBusConnection: MessageBusConnection = ideaProject.messageBus.connect()

    override var commits: Collection<Commit> = listOf()

    override var changes: Collection<Change> = listOf()

    override var comments: Collection<Comment> = listOf()

    override fun dispose() {
        messageBusConnection.disconnect()
    }
}