package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import net.ntworld.mergeRequestIntegrationIde.diff.DiffView

interface GutterIconRenderer {
    // TODO: Should be rename to visibleLineLeft + visibleLineRight
    val visibleLine: Int

    val logicalLine: Int

    val contentType: DiffView.ContentType

    fun setState(state: GutterState)

    fun triggerAddAction()

    fun triggerToggleAction()
}