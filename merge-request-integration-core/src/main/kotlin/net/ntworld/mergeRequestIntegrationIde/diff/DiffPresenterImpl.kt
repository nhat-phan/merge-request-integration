package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.util.EventDispatcher
import java.util.*

internal class DiffPresenterImpl(
    override val model: DiffModel,
    override val view: DiffView<*>
) : DiffPresenter, DiffView.Action {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        view.dispatcher.addListener(this)

        println(this)
    }

}