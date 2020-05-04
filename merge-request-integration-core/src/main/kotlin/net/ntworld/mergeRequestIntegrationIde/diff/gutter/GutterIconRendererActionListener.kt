package net.ntworld.mergeRequestIntegrationIde.diff.gutter

interface GutterIconRendererActionListener {
    fun performGutterIconRendererAction(gutterIconRenderer: GutterIconRenderer, type: GutterActionType)
}