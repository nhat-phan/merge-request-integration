package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.editor.ex.EditorEx
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

object ThreadFactory {
    fun makeModel(comments: List<Comment>): ThreadModel {
        return ThreadModelImpl(comments, false)
    }

    fun makeView(
        applicationService: ApplicationService,
        editor: EditorEx,
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        position: GutterPosition
    ): ThreadView {
        return ThreadViewImpl(
            applicationService, editor, providerData, mergeRequestInfo, logicalLine, contentType, position
        )
    }

    fun makePresenter(model: ThreadModel, view: ThreadView): ThreadPresenter {
        return ThreadPresenterImpl(model, view)
    }
}