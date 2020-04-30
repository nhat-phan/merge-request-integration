package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import javax.swing.JComponent
import javax.swing.JPanel

class ProviderCollectionToolbar : ProviderCollectionToolbarUI, Component {
    override val eventDispatcher = EventDispatcher.create(ProviderCollectionToolbarEventListener::class.java)

    private val myPanel by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left, fill]push[right]", "center"))

        val mainActionGroup = DefaultActionGroup()
        mainActionGroup.add(myRefreshAction)
        // mainActionGroup.addSeparator()
        // mainActionGroup.add(myAddAction)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "${ProviderCollectionToolbar::class.java.canonicalName}/toolbar-left", mainActionGroup, true
        )

        val rightCornerActionGroup = DefaultActionGroup()
        // rightCornerActionGroup.add(myHelpAction)
        val rightCornerToolbar = ActionManager.getInstance().createActionToolbar(
            "${ProviderCollectionToolbar::class.java.canonicalName}/toolbar-right", rightCornerActionGroup, true
        )

        panel.add(toolbar.component)
        panel.add(rightCornerToolbar.component)
        panel
    }

    private class MyHelpAction(private val self: ProviderCollectionToolbar) :
        AnAction("Help", null, AllIcons.Actions.Help) {
        override fun actionPerformed(e: AnActionEvent) {
            self.eventDispatcher.multicaster.helpClicked()
        }
    }
    private val myHelpAction = MyHelpAction(this)

    private class MyRefreshAction(private val self: ProviderCollectionToolbar) :
        AnAction("Refresh", null, AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            self.eventDispatcher.multicaster.refreshClicked()
        }
    }
    private val myRefreshAction = MyRefreshAction(this)

    override fun createComponent(): JComponent = myPanel
}