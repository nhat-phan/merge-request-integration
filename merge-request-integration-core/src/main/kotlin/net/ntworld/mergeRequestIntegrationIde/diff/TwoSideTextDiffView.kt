package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class TwoSideTextDiffView(
    private val applicationService: ApplicationService,
    override val viewer: TwosideTextDiffViewer
) : DiffView<TwosideTextDiffViewer> {
    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)

    override fun displayCommentGutterIcons(comments: List<Comment>, contentType: DiffView.ContentType) {
    }

    override fun hideComments() {
    }
}