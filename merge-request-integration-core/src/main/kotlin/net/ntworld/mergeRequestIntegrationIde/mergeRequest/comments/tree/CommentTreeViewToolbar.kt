package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.treeStructure.actions.CollapseAllAction
import com.intellij.ui.treeStructure.actions.ExpandAllAction
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequestIntegrationIde.Component
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree

internal class CommentTreeViewToolbar(
    private val myTree: JTree,
    private val dispatcher: EventDispatcher<CommentTreeView.ActionListener>
) : Component {
    var showResolved: Boolean = false
    private var myMode = CommentTreeView.ToolbarMode.FULL
    private val mySkipResolvedButton = MySkipResolvedButton(this)
    private val myRefreshButton = MyRefreshButton(this)
    private val myAddGeneralComment = MyAddGeneralComment(this)

    private val myPanel by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left]push[right]", "center"))
        val mainActionGroup = DefaultActionGroup()
        mainActionGroup.add(mySkipResolvedButton)
        val mainToolbar = ActionManager.getInstance().createActionToolbar(
            "${this::class.java.canonicalName}/toolbar",
            mainActionGroup,
            true
        )

        val rightActionGroup = DefaultActionGroup()
        rightActionGroup.add(myRefreshButton)
        rightActionGroup.addSeparator()
        rightActionGroup.add(ExpandAllAction(myTree))
        rightActionGroup.add(CollapseAllAction(myTree))
        rightActionGroup.addSeparator()
        rightActionGroup.add(myAddGeneralComment)
        val rightToolbar = ActionManager.getInstance().createActionToolbar(
            "${this::class.java.canonicalName}/toolbar-right",
            rightActionGroup,
            true
        )

        panel.add(mainToolbar.component)
        panel.add(rightToolbar.component)
        panel
    }

    override val component: JComponent = myPanel

    fun setMode(mode: CommentTreeView.ToolbarMode) {
        if (mode != myMode) {
            myMode = mode
        }
    }

    private class MySkipResolvedButton(private val self: CommentTreeViewToolbar) :
        ToggleAction("Show resolved comments", "Show resolved comments", null) {
        override fun isSelected(e: AnActionEvent): Boolean {
            return self.showResolved
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            self.showResolved = state
            self.dispatcher.multicaster.onShowResolvedCommentsToggled(self.showResolved)
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            if (self.myMode == CommentTreeView.ToolbarMode.FULL) {
                e.presentation.icon = null
            } else {
                e.presentation.icon = Icons.Resolved
            }
        }

        override fun displayTextInToolbar(): Boolean {
            return self.myMode == CommentTreeView.ToolbarMode.FULL
        }
        override fun useSmallerFontForTextInToolbar() = true
    }

    private class MyRefreshButton(private val self: CommentTreeViewToolbar) :
        AnAction("Refresh", "Refresh comment list", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            self.dispatcher.multicaster.onRefreshButtonClicked()
        }
    }

    private class MyAddGeneralComment(private val self: CommentTreeViewToolbar) : AnAction(
        "General Comment", "Add a general comment", AllIcons.General.Add
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            self.dispatcher.multicaster.onCreateGeneralCommentClicked()
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            if (self.myMode == CommentTreeView.ToolbarMode.FULL) {
                e.presentation.text = "General Comment"
            } else {
                e.presentation.text = "Add a general comment"
            }
        }

        override fun displayTextInToolbar(): Boolean {
            return self.myMode == CommentTreeView.ToolbarMode.FULL
        }
        override fun useSmallerFontForTextInToolbar() = true
    }
}