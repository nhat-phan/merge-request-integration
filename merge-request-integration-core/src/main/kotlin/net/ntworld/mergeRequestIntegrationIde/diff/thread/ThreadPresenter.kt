package net.ntworld.mergeRequestIntegrationIde.diff.thread

import net.ntworld.mergeRequestIntegrationIde.Presenter
import java.util.*

interface ThreadPresenter : Presenter<ThreadPresenter.Event> {
    val model: ThreadModel

    val view: ThreadView

    interface Event: EventListener {
    }
}