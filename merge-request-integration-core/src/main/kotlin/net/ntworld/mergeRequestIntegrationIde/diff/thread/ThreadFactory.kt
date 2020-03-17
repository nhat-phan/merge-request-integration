package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.editor.ex.EditorEx
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.AddCommentRequestedPosition

object ThreadFactory {
    fun makeModel(comments: List<Comment>): ThreadModel {
        return ThreadModelImpl(comments, true)
    }

    fun makeView(
        editor: EditorEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        position: AddCommentRequestedPosition
    ): ThreadView {
        return ThreadViewImpl(editor, providerData, mergeRequest, logicalLine, position)
    }

    fun makePresenter(model: ThreadModel, view: ThreadView): ThreadPresenter {
        return ThreadPresenterImpl(model, view)
    }
}