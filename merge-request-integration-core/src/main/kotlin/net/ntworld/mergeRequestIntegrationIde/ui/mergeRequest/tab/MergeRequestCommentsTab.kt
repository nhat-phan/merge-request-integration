package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.OnePixelSplitter
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment.CommentCollection
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment.CommentCollectionUI
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment.CommentDetails
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment.CommentDetailsUI
import javax.swing.JComponent

class MergeRequestCommentsTab(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject
) : MergeRequestCommentsTabUI {
    override val dispatcher = EventDispatcher.create(MergeRequestCommentsTabUI.Listener::class.java)

    private val mySplitter = OnePixelSplitter(
        MergeRequestCommentsTab::class.java.canonicalName,
        0.5f
    )
    private val myCollection : CommentCollectionUI = CommentCollection(applicationService, ideaProject)
    private val myCollectionEventListener = object: CommentCollectionUI.Listener {
        override fun commentUnselected() {
            myDetails.hide()
        }

        override fun commentSelected(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) {
            myDetails.displayComment(providerData, mergeRequest, comment)
        }

        override fun editorSelected(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comment: Comment?,
            item: CommentStore.Item
        ) {
            myDetails.showForm(providerData, mergeRequest, comment, item)
        }

        override fun commentsDisplayed(total: Int) {
            dispatcher.multicaster.commentsDisplayed(total)
        }

        override fun refreshRequested(mergeRequest: MergeRequest) {
            dispatcher.multicaster.refreshRequested(mergeRequest)
        }
    }
    private val myDetails: CommentDetailsUI = CommentDetails(applicationService, ideaProject)
    private val myDetailsEventListener = object: CommentDetailsUI.Listener {
        override fun onRefreshCommentsRequested(mergeRequest: MergeRequest) {
            myCollection.dispatcher.multicaster.refreshRequested(mergeRequest)
        }

        override fun onReplyButtonClicked() {
            myCollection.createReplyCommentNode()
        }
    }

    init {
        mySplitter.firstComponent = myCollection.createComponent()
        mySplitter.secondComponent = myDetails.createComponent()
        myCollection.dispatcher.addListener(myCollectionEventListener)
        myDetails.dispatcher.addListener(myDetailsEventListener)
    }

    override fun setComments(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
        myDetails.hide()
        myCollection.setComments(providerData, mergeRequest, comments)
    }

    override fun createComponent(): JComponent = mySplitter
}