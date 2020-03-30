package net.ntworld.mergeRequestIntegrationIde.watcher

interface Watcher {
    val interval: Long

    fun canExecute(): Boolean

    fun shouldTerminate(): Boolean

    fun execute()

    fun terminate()
}
