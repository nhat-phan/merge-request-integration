package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequestIntegrationIde.Presenter

interface ThreadPresenter : Presenter<ThreadPresenter.EventListener>, Disposable {
    val model: ThreadModel

    val view: ThreadView

    interface EventListener: java.util.EventListener, CommentEvent
}