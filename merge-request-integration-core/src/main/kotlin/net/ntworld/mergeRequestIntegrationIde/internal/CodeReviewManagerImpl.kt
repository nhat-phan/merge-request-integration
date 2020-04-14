package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.openapi.diagnostic.Logger
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

    private val myLogger = Logger.getInstance(this.javaClass)

    private var myComments: Collection<Comment> = listOf()
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

    override var comments: Collection<Comment>
        get() = myComments
        set(value) {
            myComments = value
            buildCommentsMap(value)
        }


    private val myCommentsMap = mutableMapOf<String, MutableList<Comment>>()
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

    private fun buildCommentsMap(value: Collection<Comment>) {
        if (null === repository) {
            return
        }
        myCommentsMap.clear()
        for (comment in value) {
            val position = comment.position
            if (null === position) {
                continue
            }
            if (null !== position.newPath) {
                doHashComment(repository, position.newPath!!, comment)
            }
            if (null !== position.oldPath) {
                doHashComment(repository, position.oldPath!!, comment)
            }
        }
        myLogger.info("myCommentsMap was built successfully")
        myCommentsMap.forEach { (path, comments) ->
            val commentIds = comments.map { it.id }
            myLogger.info("$path contains ${commentIds.joinToString(",")}")
        }
    }

    private fun doHashComment(repository: GitRepository, path: String, comment: Comment) {
        val fullPath = RepositoryUtil.findAbsoluteCrossPlatformsPath(repository, path)
        val list = myCommentsMap[fullPath]
        if (null === list) {
            myCommentsMap[fullPath] = mutableListOf(comment)
        } else {
            if (!list.contains(comment)) {
                list.add(comment)
            }
        }
    }

    override fun getCommentsByPath(path: String): List<Comment> {
        val crossPlatformsPath = RepositoryUtil.transformToCrossPlatformsPath(path)
        val comments = myCommentsMap[crossPlatformsPath]
        if (null !== comments) {
            return comments
        }
        myLogger.info("There is no comments for $crossPlatformsPath")
        return listOf()
    }

    override fun dispose() {
        messageBusConnection.disconnect()
    }
}