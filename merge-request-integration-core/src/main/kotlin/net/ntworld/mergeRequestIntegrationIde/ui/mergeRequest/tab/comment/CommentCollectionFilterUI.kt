package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface CommentCollectionFilterUI : Component {
    val dispatcher: EventDispatcher<Listener>

    interface Listener : EventListener {
        fun onFiltersChanged(showResolved: Boolean)

        fun onRefreshButtonClicked()

        fun onAddGeneralCommentClicked()
    }
}