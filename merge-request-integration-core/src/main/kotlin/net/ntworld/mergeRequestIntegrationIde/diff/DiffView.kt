package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.FrameDiffTool
import com.intellij.openapi.actionSystem.AnActionEvent
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import java.util.*

interface DiffView<V : FrameDiffTool.DiffViewer> : View<DiffView.Action> {
    val viewer: V

    fun displayAddGutterIcons()

    fun displayCommentsGutterIcon(line: Int, contentType: ContentType, comments: List<Comment>)

    fun hideComments()

    enum class ContentType {
        BEFORE,
        AFTER
    }

    interface Action : EventListener {
        fun onInit()

        fun onDispose()

        fun onBeforeRediff()

        fun onAfterRediff()

        fun onRediffAborted()

        fun onAddGutterIconClicked(renderer: AddGutterIconRenderer, e: AnActionEvent?)

        fun onCommentsGutterIconClicked(renderer: CommentsGutterIconRenderer, e: AnActionEvent)
    }
}
