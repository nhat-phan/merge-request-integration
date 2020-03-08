package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.FrameDiffTool
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import java.util.*

interface DiffView<V : FrameDiffTool.DiffViewer> : View<DiffView.Action> {
    val viewer: V

    fun displayCommentGutterIcons(comments: List<Comment>, contentType: ContentType)

    fun hideComments()

    enum class ContentType {
        BEFORE,
        AFTER
    }

    interface Action : EventListener {
    }
}
