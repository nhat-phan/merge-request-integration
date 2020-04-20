package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.Panel
import com.intellij.ui.tabs.TabInfo
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeFactory
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreePresenter
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.component.comment.ComponentFactory
import net.ntworld.mergeRequestIntegrationIde.component.comment.EditorComponent
import net.ntworld.mergeRequestIntegrationIde.component.comment.GroupComponent
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil
import java.awt.event.ComponentEvent
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel

class CommentsTabViewImpl(
    private val projectService: ProjectService,
    private val providerData: ProviderData
) : AbstractView<CommentsTabView.ActionListener>(), CommentsTabView {
    override val dispatcher = EventDispatcher.create(CommentsTabView.ActionListener::class.java)

    private val mySplitter = OnePixelSplitter(
        this::class.java.canonicalName,
        0.5f
    )
    private val myTreePresenter: CommentTreePresenter = CommentTreeFactory.makePresenter(
        CommentTreeFactory.makeModel(providerData),
        CommentTreeFactory.makeView(projectService, providerData)
    )
    private val myTreeListener = object : CommentTreePresenter.Listener {
        override fun onTreeNodeSelected(node: Node) {
            dispatcher.multicaster.onTreeNodeSelected(node)
        }

        override fun onShowResolvedCommentsToggled(displayResolvedComments: Boolean) {
            dispatcher.multicaster.onShowResolvedCommentsToggled(displayResolvedComments)
        }

        override fun onCreateGeneralCommentClicked() {
            dispatcher.multicaster.onCreateGeneralCommentClicked()
        }

        override fun onRefreshButtonClicked() {
            dispatcher.multicaster.onRefreshButtonClicked()
        }
    }
    private var myCommentPosition: CommentPosition? = null
    private val myGroupCommentCollection = mutableListOf<GroupComponent>()
    private val myThreadPanel = Panel()
    private val myThreadBoxLayout = JBUI.Panels.simplePanel()
    private val myThreadWrapper = ScrollPaneFactory.createScrollPane(myThreadBoxLayout, true)
    private val myMainEditorEventListener = object : EditorComponent.EventListener {
        override fun onEditorFocused(editor: EditorComponent) {
            myThreadWrapper.verticalScrollBar.value = myThreadWrapper.verticalScrollBar.maximum
        }

        override fun onEditorResized(editor: EditorComponent) {}

        override fun onCancelClicked(editor: EditorComponent) {
            if (editor.text.isNotBlank()) {
                val result = Messages.showYesNoDialog(
                    "Do you want to delete the whole content?", "Are you sure", Messages.getQuestionIcon()
                )
                result == Messages.YES
            }
            editor.text = ""
        }

        override fun onSubmitClicked(editor: EditorComponent) {
            val text = editor.text.trim()
            val position = myCommentPosition
            if (text.isNotEmpty()) {
                dispatcher.multicaster.onCreateCommentRequested(editor.text, position)
            }
        }
    }
    private val myMainEditor by lazy {
        val editor = ComponentFactory.makeEditor(
            projectService.project, EditorComponent.Type.NEW_DISCUSSION, 0, 0, false
        )
        editor.isVisible = true
        editor.addListener(myMainEditorEventListener)

        editor
    }
    private val myGroupComponentEventListener = object : GroupComponent.EventListener {
        override fun onResized() {}

        override fun onEditorCreated(groupId: String, editor: EditorComponent) {
        }

        override fun onEditorDestroyed(groupId: String, editor: EditorComponent) {
        }

        override fun onReplyCommentRequested(comment: Comment, content: String) {
            if (content.trim().isNotEmpty()) {
                dispatcher.multicaster.onReplyCommentRequested(comment, content)
            }
        }

        override fun onDeleteCommentRequested(comment: Comment) {
            dispatcher.multicaster.onDeleteCommentRequested(comment)
        }

        override fun onResolveCommentRequested(comment: Comment) {
            dispatcher.multicaster.onResolveCommentRequested(comment)
        }

        override fun onUnresolveCommentRequested(comment: Comment) {
            dispatcher.multicaster.onUnresolveCommentRequested(comment)
        }
    }

    override val component: JComponent = mySplitter

    override val tabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(component)
        tabInfo.text = "Comments"
        tabInfo.icon = Icons.Comments
        tabInfo
    }

    init {
        myThreadPanel.background = JBColor.border()
        myThreadBoxLayout.background = JBColor.border()
        myThreadWrapper.background = JBColor.border()

        myThreadBoxLayout.addToCenter(myThreadPanel)
        myThreadPanel.layout = BoxLayout(myThreadPanel, BoxLayout.Y_AXIS)
        myThreadBoxLayout.addToBottom(myMainEditor.component)

        mySplitter.firstComponent = myTreePresenter.component
        mySplitter.secondComponent = null

        myTreePresenter.addListener(myTreeListener)
    }

    override fun displayCommentCount(count: Int) {
        tabInfo.text = if (0 == count) "Comments" else "Comments · $count"
    }

    override fun dispose() {
    }

    override fun renderTree(
        mergeRequestInfo: MergeRequestInfo, comments: List<Comment>, displayResolvedComments: Boolean
    ) {
        myTreePresenter.model.mergeRequestInfo = mergeRequestInfo
        myTreePresenter.model.comments = comments
        myTreePresenter.model.displayResolvedComments = displayResolvedComments
    }

    override fun hideThread() {
        mySplitter.secondComponent = null
    }

    override fun renderThread(mergeRequestInfo: MergeRequestInfo, groupedComments: Map<String, List<Comment>>) {
        myThreadPanel.removeAll()
        myGroupCommentCollection.forEach { it.dispose() }
        myGroupCommentCollection.clear()
        myCommentPosition = null

        groupedComments.forEach { (groupId, comments) ->
            if (comments.isEmpty()) {
                return@forEach
            }

            val group = ComponentFactory.makeGroup(
                providerData, mergeRequestInfo, projectService.project, false, groupId, comments, 0
            )
            myCommentPosition = comments.first().position

            group.addListener(myGroupComponentEventListener)
            myThreadPanel.add(group.component)
            myGroupCommentCollection.add(group)
        }

        if (null !== myCommentPosition) {
            myMainEditor.addCommentButtonText = "Add comment"
            myMainEditor.addCommentButtonDesc = "Add comment to current line"
        } else {
            myMainEditor.addCommentButtonText = "Add general comment"
            myMainEditor.addCommentButtonDesc = "Create new thread of general comment"
        }
        mySplitter.secondComponent = myThreadWrapper
    }

    override fun hasGeneralCommentsTreeNode(): Boolean = myTreePresenter.hasGeneralCommentsTreeNode()

    override fun selectGeneralCommentsTreeNode() = myTreePresenter.selectGeneralCommentsTreeNode()

    override fun focusToMainEditor() = myMainEditor.focus()

    override fun clearMainEditorText() {
        myMainEditor.text = ""
    }
}