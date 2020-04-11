package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface CommentsTabModel : Model<CommentsTabModel.DataListener> {

    interface DataListener : EventListener {}
}