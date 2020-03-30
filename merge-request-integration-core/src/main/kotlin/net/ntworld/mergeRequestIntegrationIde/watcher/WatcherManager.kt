package net.ntworld.mergeRequestIntegrationIde.watcher

import com.intellij.openapi.Disposable

interface WatcherManager : Disposable {
    fun addWatcher(watcher: Watcher)

    fun removeWatcher(watcher: Watcher)
}
