package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil
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

    private var myChanges: Collection<Change> = listOf()
    private var myCommits: Collection<Commit> = listOf()

    override var commits: Collection<Commit>
        get() = myCommits
        set(value) {
            myCommits = value
        }

    override var changes: Collection<Change>
        get() = myChanges
        set(value) {
            myChanges = value
            buildChangesMap(value)
        }

    override var comments: Collection<Comment> = listOf()

    private val myChangesMap = mutableMapOf<String, MutableList<Change>>()

    private fun buildChangesMap(value: Collection<Change>) {
        myChangesMap.clear()
        for (change in value) {
            val filePaths = ChangesUtil.getPathsCaseSensitive(change)
            for (filePath in filePaths) {
                val path = filePath.path
                val list = myChangesMap.get(path)
                if (null === list) {
                    myChangesMap[path] = mutableListOf(change)
                } else {
                    if (!list.contains(change)) {
                        list.add(change)
                    }
                }
            }
        }
    }

    override fun dispose() {
        messageBusConnection.disconnect()
    }
}