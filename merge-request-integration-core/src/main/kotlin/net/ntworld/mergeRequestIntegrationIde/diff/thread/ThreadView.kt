package net.ntworld.mergeRequestIntegrationIde.diff.thread

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import java.util.*

interface ThreadView : View<ThreadView.Action> {
    fun initialize()

    fun dispose()

    fun addCommentPanel(comment: Comment)

    fun show()

    fun hide()

    interface Action : EventListener {}
}