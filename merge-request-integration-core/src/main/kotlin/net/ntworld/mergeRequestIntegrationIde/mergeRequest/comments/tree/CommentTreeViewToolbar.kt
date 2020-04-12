package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequestIntegrationIde.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment.CommentCollectionFilter
import javax.swing.JComponent
import javax.swing.JPanel

internal class CommentTreeViewToolbar(
    private val dispatcher: EventDispatcher<CommentTreeView.ActionListener>
) : Component {
    var showResolved: Boolean = false

    private val mySkipResolvedButton = object : ToggleAction(
        "Show resolved comments", "Show resolved comments", null
    ) {
        override fun isSelected(e: AnActionEvent): Boolean {
            return showResolved
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            showResolved = state
            dispatcher.multicaster.onShowResolvedCommentsToggled(showResolved)
        }

        override fun displayTextInToolbar() = true
        override fun useSmallerFontForTextInToolbar() = true
    }
    private val myRefreshButton = object : AnAction("Refresh", "Refresh comment list", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            dispatcher.multicaster.onRefreshButtonClicked()
        }
    }
    private val myAddGeneralComment = object : AnAction(
        "General Comment", "Add a general comment", AllIcons.General.Add
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            dispatcher.multicaster.onCreateGeneralCommentClicked()
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar() = true
    }

    private val myPanel by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left]push[right]", "center"))
        val mainActionGroup = DefaultActionGroup()
        mainActionGroup.add(mySkipResolvedButton)
        val mainToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommentCollectionFilter::class.java.canonicalName}/toolbar",
            mainActionGroup,
            true
        )

        val rightCornerActionGroup = DefaultActionGroup()
        rightCornerActionGroup.add(myRefreshButton)
        rightCornerActionGroup.addSeparator()
        rightCornerActionGroup.add(myAddGeneralComment)
        val rightCornerToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommentCollectionFilter::class.java.canonicalName}/toolbar-right",
            rightCornerActionGroup,
            true
        )

        panel.add(mainToolbar.component)
        panel.add(rightCornerToolbar.component)
        panel
    }

    override val component: JComponent = myPanel
}