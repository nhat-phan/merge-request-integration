package net.ntworld.mergeRequestIntegrationIde.watcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatcherManagerImplTest {

    @Test
    fun testCanScheduleInfinityWatcher() {
        var count = 0
        class MyWatcher : Watcher {
            override val interval: Long = 100

            override fun canExecute() = true
            override fun shouldTerminate() = false

            override fun execute() {
                count++
            }

            override fun terminate() {
            }
        }
        val watcherManager = WatcherManagerImpl()

        watcherManager.addWatcher(MyWatcher())

        Thread.sleep(950)
        assertTrue(count >= 10)
    }

    @Test
    fun testCanTerminateTheWatcherByShouldTerminateCondition() {
        var count = 0
        class MyWatcher : Watcher {
            override val interval: Long = 100

            override fun canExecute() = true
            override fun shouldTerminate() = count == 5

            override fun execute() {
                count++
            }

            override fun terminate() {
                count += 10
            }
        }
        val watcherManager = WatcherManagerImpl()

        watcherManager.addWatcher(MyWatcher())

        Thread.sleep(950)
        assertEquals(15, count)
    }

    @Test
    fun testNotTriggerExecuteIfThereIsCanExecuteReturnFalse() {
        var count = 0
        class MyWatcher : Watcher {
            override val interval: Long = 100

            override fun canExecute() = false
            override fun shouldTerminate() = count == 5

            override fun execute() {
                count++
            }

            override fun terminate() {
            }
        }
        val watcherManager = WatcherManagerImpl()

        watcherManager.addWatcher(MyWatcher())

        Thread.sleep(950)
        assertEquals(0, count)
    }
}