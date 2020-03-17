package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import java.util.*

interface ThreadView : View<ThreadView.Action>, Disposable {
    fun initialize()

    fun addCommentPanel(comment: Comment)

    fun show()

    fun hide()

    interface Action : EventListener {}
}