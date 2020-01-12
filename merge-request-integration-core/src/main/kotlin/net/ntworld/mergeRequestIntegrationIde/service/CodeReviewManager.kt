package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint

interface CodeReviewManager : CodeReviewUtil {
    val providerData: ProviderData
    val mergeRequest: MergeRequest
    val repository: GitRepository?

    var commits: Collection<Commit>
    var changes: Collection<Change>
    var comments: Collection<Comment>

    fun findChangeInfoByPathAndContent(path: String, content: String): ChangeInfo?

    fun findCommentPoints(path: String, changeInfo: ChangeInfo): List<CommentPoint>

    // fun getCommentsForChange(change: Change)

    fun findCommentPosition(editor: Editor, caret: Caret?, dataContext: DataContext?) : CommentPosition?

    interface ChangeInfo {
        val change: Change

        val contentRevision: ContentRevision

        val before: Boolean

        val after: Boolean
    }
}