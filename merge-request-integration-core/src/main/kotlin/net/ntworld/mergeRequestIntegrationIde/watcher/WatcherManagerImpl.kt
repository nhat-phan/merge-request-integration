package net.ntworld.mergeRequestIntegrationIde.watcher

import java.util.*

class WatcherManagerImpl : WatcherManager {
    private val myWatchers = mutableMapOf<Watcher, Timer>()

    override fun addWatcher(watcher: Watcher) {
        val timer = Timer()
        timer.schedule(MyTimerTask(this, watcher), 0L, watcher.interval)

        myWatchers[watcher] = timer
    }

    override fun removeWatcher(watcher: Watcher) {
        val timer = myWatchers[watcher]
        if (null !== timer) {
            timer.cancel()
            myWatchers.remove(watcher)
        }
    }

    override fun dispose() {
        myWatchers.forEach {
            it.value.cancel()
        }
        myWatchers.clear()
    }

    internal class MyTimerTask(
        private val watcherManager: WatcherManager,
        private val watcher: Watcher
    ): TimerTask() {
        override fun run() {
            if (watcher.canExecute()) {
                watcher.execute()
            }

            if (watcher.shouldTerminate()) {
                watcher.terminate()
                watcherManager.removeWatcher(watcher)
            }
        }
    }
}