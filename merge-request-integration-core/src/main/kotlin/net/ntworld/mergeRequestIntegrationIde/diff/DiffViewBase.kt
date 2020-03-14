package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.util.EventDispatcher

abstract class DiffViewBase<V : DiffViewerBase>(
    private val viewerBase: DiffViewerBase
) : DiffView<V> {
    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)

    private val diffViewerListener = object : DiffViewerListener() {
        override fun onInit() = dispatcher.multicaster.onInit()
        override fun onDispose() = dispatcher.multicaster.onDispose()
        override fun onBeforeRediff() = dispatcher.multicaster.onBeforeRediff()
        override fun onAfterRediff() = dispatcher.multicaster.onAfterRediff()
        override fun onRediffAborted() = dispatcher.multicaster.onRediffAborted()
    }

    init {
        viewerBase.addListener(diffViewerListener)
    }
}