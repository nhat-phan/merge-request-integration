package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.treeStructure.actions.CollapseAllAction
import com.intellij.ui.treeStructure.actions.ExpandAllAction
import java.awt.Component
import javax.swing.JTree

object ToolbarUtil {
    fun createExpandAndCollapseToolbar(name: String, tree: JTree): Component {
        val actionGroup = DefaultActionGroup()
        actionGroup.add(ExpandAllAction(tree))
        actionGroup.add(CollapseAllAction(tree))

        val toolbar = ActionManager.getInstance().createActionToolbar(
            name,
            actionGroup,
            true
        )
        return toolbar.component
    }
}