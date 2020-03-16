package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.tools.fragmented.UnifiedFragmentBuilder
import com.intellij.diff.util.DiffUtil
import com.intellij.diff.util.Side
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.PreviewDiffVirtualFile
import git4idea.repo.GitRepository
import gnu.trove.TIntFunction
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import net.ntworld.mergeRequestIntegrationIde.ui.editor.EditorUtil
import net.ntworld.mergeRequestIntegrationIde.ui.service.DisplayChangesService
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import java.lang.Exception

internal class CodeReviewManagerImpl(
    private val ideaProject: IdeaProject,
    override val providerData: ProviderData,
    override val mergeRequest: MergeRequest,
    val util: CodeReviewUtil
) : CodeReviewManager, CodeReviewUtil by util {
    override val repository: GitRepository? = RepositoryUtil.findRepository(ideaProject, providerData)

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
    }

    private fun doHashComment(repository: GitRepository, path: String, comment: Comment) {
        val fullPath = RepositoryUtil.findAbsolutePath(repository, path)
        val list = myCommentsMap[fullPath]
        if (null === list) {
            myCommentsMap[fullPath] = mutableListOf(comment)
        } else {
            if (!list.contains(comment)) {
                list.add(comment)
            }
        }
    }

    override fun findChangeInfoByPathAndContent(path: String, content: String): CodeReviewManager.ChangeInfo? {
        val changes = myChangesMap[path]
        if (null === changes || changes.isEmpty()) {
            return null
        }

        for (change in changes) {
            val beforeRevision = change.beforeRevision
            if (null !== beforeRevision && compareContent(beforeRevision.content, content)) {
                return ChangeInfoImpl(
                    change = change,
                    contentRevision = beforeRevision,
                    before = true,
                    after = false
                )
            }
            val afterRevision = change.afterRevision
            if (null !== afterRevision && compareContent(afterRevision.content, content)) {
                return ChangeInfoImpl(
                    change = change,
                    contentRevision = afterRevision,
                    before = false,
                    after = true
                )
            }
        }
        return null
    }

    private fun compareContent(left: String?, right: String?): Boolean {
        // we cannot compare content directly because newline in Windows & macOS is different
        // newline in Windows is \r\n but in macOs or Linux it is \n
        // so we compare each line of the content
        if (null !== left && null !== right) {
            return StringUtil.convertLineSeparators(left).equals(
                StringUtil.convertLineSeparators(right)
            )
        }
        return left.equals(right)
    }

    override fun findCommentPoints(path: String, changeInfo: CodeReviewManager.ChangeInfo): List<CommentPoint> {
        val comments = myCommentsMap[path]
        if (null === comments || comments.isEmpty()) {
            return listOf()
        }

        val result = mutableListOf<CommentPoint>()
        for (comment in comments) {
            val position = comment.position!!
            if (changeInfo.before && null !== position.oldLine) {
                val revision = changeInfo.contentRevision.revisionNumber.asString()
                if (position.startHash == revision || position.baseHash == revision) {
                    result.add(CommentPoint(position.oldLine!!, comment))
                    continue
                }
            }
            if (changeInfo.after && null !== position.newLine) {
                val revision = changeInfo.contentRevision.revisionNumber.asString()
                if (position.headHash == revision || position.baseHash == revision) {
                    result.add(CommentPoint(position.newLine!!, comment))
                    continue
                }
            }
        }
        return result
    }

    private data class ChangeInfoImpl(
        override val change: Change,
        override val contentRevision: ContentRevision,
        override val before: Boolean,
        override val after: Boolean
    ) : CodeReviewManager.ChangeInfo

}