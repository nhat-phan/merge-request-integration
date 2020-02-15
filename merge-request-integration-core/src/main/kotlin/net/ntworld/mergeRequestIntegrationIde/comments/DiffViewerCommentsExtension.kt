package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffExtension
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.ContentDiffRequest
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.tools.util.base.DiffViewerBase


class DiffViewerCommentsExtension : DiffExtension() {

    override fun onViewerCreated(
            viewer: FrameDiffTool.DiffViewer,
            context: DiffContext,
            request: DiffRequest
    ) {
        context.project?.takeIf { viewer is DiffViewerBase && request is ContentDiffRequest }?.let {
            ReviewCommentsSupport.getInstance(it)
                    .installExtensions(viewer as DiffViewerBase, request as ContentDiffRequest)

        }
    }
}