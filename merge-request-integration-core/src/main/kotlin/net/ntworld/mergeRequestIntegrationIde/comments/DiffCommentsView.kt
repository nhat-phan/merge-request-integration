package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.diff.FrameDiffTool
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import java.util.*

interface DiffCommentsView<V: FrameDiffTool.DiffViewer> {
    val viewer: V

    val dispatcher: EventDispatcher<Listener>

    fun displayCommentGutterIcons(comments: List<Comment>, side: Side)

    fun hideComments()

    interface Listener: EventListener {
        fun onInitialized()
    }

    enum class Side {
        LEFT,
        RIGHT
    }
}