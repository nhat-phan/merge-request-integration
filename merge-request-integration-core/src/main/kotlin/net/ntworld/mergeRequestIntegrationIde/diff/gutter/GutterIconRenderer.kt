package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import net.ntworld.mergeRequestIntegrationIde.diff.DiffView

interface GutterIconRenderer {
    val visibleLineLeft: Int?

    val visibleLineRight: Int?

    val logicalLine: Int

    val contentType: DiffView.ContentType

    fun setState(state: GutterState)

    fun triggerAddAction()

    fun triggerToggleAction()
}
