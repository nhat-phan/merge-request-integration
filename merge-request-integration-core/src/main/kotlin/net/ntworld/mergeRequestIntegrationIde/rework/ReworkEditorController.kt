package net.ntworld.mergeRequestIntegrationIde.rework

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData

interface ReworkEditorController : Disposable {
    val textEditor: TextEditor

    val editor: EditorEx

    val providerData: ProviderData

    val mergeRequestInfo: MergeRequestInfo

    val path: String

    val revisionNumber: String

    fun updateComments()

    fun hideAllComments()

    fun scrollToLine(visibleLine: Int)

    fun displayCommentsOnLine(visibleLine: Int)
}