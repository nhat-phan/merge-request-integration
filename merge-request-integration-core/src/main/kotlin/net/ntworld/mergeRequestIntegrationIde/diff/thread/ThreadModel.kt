package net.ntworld.mergeRequestIntegrationIde.diff.thread

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface ThreadModel : Model<ThreadModel.Change> {
    var comments: List<Comment>

    var visible: Boolean

    interface Change : EventListener
}
