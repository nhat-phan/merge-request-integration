package net.ntworld.mergeRequestIntegrationIde.component.gutter

import com.intellij.diff.util.Side

interface GutterIconRenderer {
    val visibleLineLeft: Int?

    val visibleLineRight: Int?

    val logicalLine: Int

    val side: Side

    fun setState(state: GutterState)

    fun triggerAddAction()

    fun triggerToggleAction()
}
