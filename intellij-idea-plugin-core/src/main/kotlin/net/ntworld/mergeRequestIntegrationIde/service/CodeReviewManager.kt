package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import net.ntworld.mergeRequest.*

interface CodeReviewManager : CodeReviewUtil {
    val providerData: ProviderData
    val mergeRequest: MergeRequest
    var commits: Collection<Commit>
    var changes: Collection<Change>
    var comments: Collection<Comment>

    fun findCommentPosition(editor: Editor, caret: Caret?, dataContext: DataContext?) : CommentPosition?
}