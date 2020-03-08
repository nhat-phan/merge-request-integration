package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.diff.FrameDiffTool
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequestIntegrationIde.comments.model.DiffCommentsModel
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class AbstractDiffCommentsPresenter<V: FrameDiffTool.DiffViewer>(
    private val applicationService: ApplicationService,
    override val diffCommentsView: DiffCommentsView<V>,
    private val change: Change
) : DiffCommentsPresenter<V> {
    override val diffCommentsModel: DiffCommentsModel
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    init {
        diffCommentsView.dispatcher.addListener(this)
    }

    override fun onInitialized() {

    }
}