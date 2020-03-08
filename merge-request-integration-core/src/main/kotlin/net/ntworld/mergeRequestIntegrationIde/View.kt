package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

interface View<Action : EventListener> {
    val dispatcher: EventDispatcher<Action>

}

interface SimpleView : View<EventListener>

interface Model<Change : EventListener> {
    val dispatcher: EventDispatcher<Change>
}

interface SimpleModel : Model<EventListener>

interface Presenter<Event : EventListener> {
    val dispatcher: EventDispatcher<Event>
}

interface SimplePresenter : Presenter<EventListener>

