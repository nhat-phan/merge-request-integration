package net.ntworld.mergeRequestIntegrationIde.component.thread

import com.intellij.diff.util.Side
import com.intellij.openapi.editor.ex.EditorEx
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

object ThreadFactory {
    fun makeModel(comments: List<Comment>): ThreadModel {
        return ThreadModelImpl(comments, false)
    }

    fun makeView(
        projectServiceProvider: ProjectServiceProvider,
        editor: EditorEx,
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        logicalLine: Int,
        side: Side,
        position: GutterPosition,
        replyInDialog: Boolean = false
    ): ThreadView {
        return ThreadViewImpl(
            projectServiceProvider, editor, providerData, mergeRequestInfo, logicalLine, side, position, replyInDialog
        )
    }

    fun makePresenter(model: ThreadModel, view: ThreadView): ThreadPresenter {
        return ThreadPresenterImpl(model, view)
    }
}