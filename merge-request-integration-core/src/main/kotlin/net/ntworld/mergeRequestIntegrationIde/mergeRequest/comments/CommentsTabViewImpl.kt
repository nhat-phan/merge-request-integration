package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.ui.JBColor
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.Panel
import com.intellij.ui.tabs.TabInfo
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeFactory
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreePresenter
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.component.comment.ComponentFactory
import net.ntworld.mergeRequestIntegrationIde.component.comment.EditorComponent
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
    private val myThreadPanel = Panel()
    private val myThreadBoxLayout = JBUI.Panels.simplePanel()
    private val myThreadWrapper = ScrollPaneFactory.createScrollPane(myThreadBoxLayout, true)
    private val myMainEditor by lazy {
        val editor = ComponentFactory.makeEditor(projectService.project, EditorComponent.Type.NEW_DISCUSSION, 0, 0)
        editor.isVisible = true

        editor
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
        groupedComments.forEach { (groupId, comments) ->
            val group = ComponentFactory.makeGroup(
                providerData, mergeRequestInfo, projectService.project, false, groupId, comments, 0
            )
            myThreadPanel.add(group.component)
        }
        mySplitter.secondComponent = myThreadWrapper
    }

    override fun selectGeneralCommentsTreeNode() = myTreePresenter.selectGeneralCommentsTreeNode()

    override fun focusToMainEditor() = myMainEditor.focus()
}