package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.diff.FrameDiffTool
import net.ntworld.mergeRequestIntegrationIde.comments.model.DiffCommentsModel

interface DiffCommentsPresenter<V: FrameDiffTool.DiffViewer>: DiffCommentsView.Listener {
    val diffCommentsModel: DiffCommentsModel

    val diffCommentsView: DiffCommentsView<V>
}

