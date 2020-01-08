package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import javax.swing.JComponent
import javax.swing.JPanel

class CommentCollectionFilter : CommentCollectionFilterUI {
    override val dispatcher = EventDispatcher.create(CommentCollectionFilterUI.Listener::class.java)
    var showResolved: Boolean = false

    private val mySkipResolvedButton = object : ToggleAction(
        "Show resolved comments", "Show resolved comments", null
    ) {
        override fun isSelected(e: AnActionEvent): Boolean {
            return showResolved
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            showResolved = state
            dispatcher.multicaster.onFiltersChanged(state)
        }

        override fun displayTextInToolbar() = true
        override fun useSmallerFontForTextInToolbar() = true
    }
    private val myRefreshButton = object : AnAction("Refresh", "Refresh comment list", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            dispatcher.multicaster.onRefreshButtonClicked()
        }
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
        val rightCornerToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommentCollectionFilter::class.java.canonicalName}/toolbar-right",
            rightCornerActionGroup,
            true
        )

        panel.add(mainToolbar.component)
        panel.add(rightCornerToolbar.component)
        panel
    }

    override fun createComponent(): JComponent = myPanel
}