package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment

import com.intellij.util.EventDispatcher
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.panel.CommentEditorPanel
import net.ntworld.mergeRequestIntegrationIde.ui.panel.CommentPanel
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class CommentDetails(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject
) : CommentDetailsUI {
    override val dispatcher = EventDispatcher.create(CommentDetailsUI.Listener::class.java)

    private val myCommentPanel = CommentPanel(applicationService)
    private val myCommentPanelComponent = myCommentPanel.createComponent()
    private val myCommentEditorPanelMap = mutableMapOf<CommentStore.Item, CommentEditorPanel>()
    private val myCommentEditorPanelComponentMap = mutableMapOf<CommentStore.Item, Component>()
    private val myWrapper = JPanel()
    private val myCommentEditorPanelListener = object : CommentEditorPanel.Listener {
        override fun onCancelButtonClicked(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comment: Comment?,
            item: CommentStore.Item
        ) {
            removeEditorPanelAndCommentInStore(item)
            dispatcher.multicaster.onRefreshCommentsRequested(mergeRequest, null)
        }

        override fun onCommentCreated(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comment: Comment?,
            item: CommentStore.Item,
            createdCommentId: String?
        ) {
            removeEditorPanelAndCommentInStore(item)
            dispatcher.multicaster.onRefreshCommentsRequested(mergeRequest, createdCommentId)
        }

        private fun removeEditorPanelAndCommentInStore(item: CommentStore.Item) {
            val component = myCommentEditorPanelComponentMap[item]
            if (null !== component) {
                myWrapper.remove(component)
                component.isVisible = false
            }
            myCommentEditorPanelMap.remove(item)
            myCommentEditorPanelComponentMap.remove(item)
            applicationService.getProjectService(ideaProject).commentStore.remove(item.id)
        }
    }
    private val myCommentPanelListener = object : CommentPanel.Listener {
        override fun onReplyButtonClick() {
            dispatcher.multicaster.onReplyButtonClicked()
        }

        override fun onResolveButtonClicked(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) =
            destroyCommentPanel(mergeRequest)

        override fun onDeleteButtonClicked(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) =
            destroyCommentPanel(mergeRequest)

        private fun destroyCommentPanel(mergeRequest: MergeRequest) {
            dispatcher.multicaster.onRefreshCommentsRequested(mergeRequest, null)
            hide()
        }
    }

    init {
        myCommentPanelComponent.isVisible = false
        myCommentEditorPanelComponentMap.values.forEach {
            it.isVisible = false
        }

        myWrapper.layout = BoxLayout(myWrapper, BoxLayout.Y_AXIS)
        myWrapper.add(myCommentPanelComponent)
        myCommentPanel.dispatcher.addListener(myCommentPanelListener)
    }

    override fun hide() {
        myCommentPanelComponent.isVisible = false
        myCommentEditorPanelComponentMap.values.forEach {
            it.isVisible = false
        }
    }

    override fun displayComment(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) {
        myCommentPanel.setComment(providerData, mergeRequest, comment)

        myCommentPanelComponent.isVisible = true
        myCommentEditorPanelComponentMap.values.forEach {
            it.isVisible = false
        }
    }

    override fun showForm(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        comment: Comment?,
        item: CommentStore.Item
    ) {
        myCommentEditorPanelComponentMap.values.forEach {
            it.isVisible = false
        }

        if (!myCommentEditorPanelMap.containsKey(item)) {
            val commentEditorPanel = CommentEditorPanel(
                applicationService, ideaProject, providerData, mergeRequest, comment, item
            )
            commentEditorPanel.addDestroyListener(myCommentEditorPanelListener)
            val commentEditorPanelComponent = commentEditorPanel.createComponent()

            myCommentEditorPanelMap[item] = commentEditorPanel
            myCommentEditorPanelComponentMap[item] = commentEditorPanelComponent
            myWrapper.add(commentEditorPanelComponent)
        }

        myCommentEditorPanelComponentMap.forEach {
            if (it.key === item) {
                myCommentEditorPanelMap[item]!!.grabFocus()
                it.value.isVisible = true
            }
        }
        myCommentPanelComponent.isVisible = false
    }

    override fun createComponent(): JComponent = myWrapper
}