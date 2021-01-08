package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.AbstractModel
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.Empty
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

class CommentsTabModelImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val providerData: ProviderData
) : AbstractModel<CommentsTabModel.DataListener>(), CommentsTabModel {
    override val dispatcher = EventDispatcher.create(CommentsTabModel.DataListener::class.java)

    private val myComments: MutableList<Comment> = mutableListOf()

    override var comments: MutableList<Comment> = mutableListOf()
        private set

    override var mergeRequestInfo: MergeRequestInfo = MergeRequestInfo.Empty
        set(value) {
            field = value
            dispatcher.multicaster.onMergeRequestInfoChanged()
        }

    override var displayResolvedComments: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                buildComments()
                dispatcher.multicaster.onCommentsUpdated(DataChangedSource.UI)
            }
        }

    override var onlyShowDraftComments: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                buildComments()
                dispatcher.multicaster.onCommentsUpdated(DataChangedSource.UI)
            }
        }

    private val myMessageBusConnection = projectServiceProvider.messageBus.connect()
    private val myMergeRequestDataNotifier = object : MergeRequestDataNotifier {
        override fun fetchCommentsRequested(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
        }

        override fun onCommentsUpdated(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            comments: List<Comment>
        ) {
            val currentProviderData = this@CommentsTabModelImpl.providerData
            val currentMergeRequestInfo = this@CommentsTabModelImpl.mergeRequestInfo
            if (providerData.id != currentProviderData.id ||
                mergeRequestInfo.id != currentMergeRequestInfo.id) {
                return
            }
            myComments.clear()
            myComments.addAll(comments)
            buildComments()
            dispatcher.multicaster.onCommentsUpdated(DataChangedSource.NOTIFIER)
        }
    }

    init {
        myMessageBusConnection.subscribe(MergeRequestDataNotifier.TOPIC, myMergeRequestDataNotifier)
    }

    override fun dispose() {
        myMessageBusConnection.disconnect()
    }

    private fun buildComments() {
        comments.clear()
        if (onlyShowDraftComments) {
            comments.addAll(myComments.filter { it.isDraft })
        } else {
            if (displayResolvedComments) {
                comments.addAll(myComments)
            } else {
                comments.addAll(myComments.filter { !it.resolved })
            }
        }
    }
}