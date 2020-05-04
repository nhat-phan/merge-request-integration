package net.ntworld.mergeRequestIntegrationIde.component.gutter

interface GutterIconRendererActionListener {
    fun performGutterIconRendererAction(gutterIconRenderer: GutterIconRenderer, type: GutterActionType)
}