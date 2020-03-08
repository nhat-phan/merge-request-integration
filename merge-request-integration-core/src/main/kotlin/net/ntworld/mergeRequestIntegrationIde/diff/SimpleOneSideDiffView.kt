package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class SimpleOneSideDiffView(
    private val applicationService: ApplicationService,
    override val viewer: SimpleOnesideDiffViewer
) : DiffView<SimpleOnesideDiffViewer> {
    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)

    override fun displayCommentGutterIcons(comments: List<Comment>, contentType: DiffView.ContentType) {
    }

    override fun hideComments() {
    }
}