package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.watcher.Watcher

object ReviewContextManager : Watcher {
    private val myLogger = Logger.getInstance(this.javaClass)
    private val myContexts = mutableMapOf<String, ReviewContextImpl>()
    private var mySelected: String? = null
    override val interval: Long = 60000

    private fun keyOf(providerId: String, mergeRequestId: String) = "$providerId:$mergeRequestId"

    fun initContext(
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        selected: Boolean
    ) {
        val key = keyOf(providerData.id, mergeRequestInfo.id)
        myLogger.info("Init context $key")
        if (!myContexts.contains(key)) {
            myContexts[key] = ReviewContextImpl(
                ideaProject, providerData, mergeRequestInfo, ideaProject.messageBus.connect()
            )
        }
        if (selected) {
            mySelected = key
        }
    }

    fun findSelectedContext() : ReviewContext? {
        val key = mySelected
        if (null === key) {
            return null
        }
        return myContexts[key]
    }

    fun findContext(providerId: String, mergeRequestId: String): ReviewContext? {
        return myContexts[keyOf(providerId, mergeRequestId)]
    }

    fun setSelected(providerId: String, mergeRequestId: String) {
        mySelected = keyOf(providerId, mergeRequestId)
    }

    fun updateComments(providerId: String, mergeRequestId: String, comments: List<Comment>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update comments for $key")
            context.comments = comments
        }
    }

    fun updateCommits(providerId: String, mergeRequestId: String, commits: List<Commit>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update commits for $key")
            context.commits = commits
        }
    }

    fun updateMergeRequest(providerId: String, mergeRequest: MergeRequest) {
        val key = keyOf(providerId, mergeRequest.id)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update diffReference for $key")
            context.diffReference = mergeRequest.diffReference
        }
    }

    override fun canExecute(): Boolean {
        return myContexts.isNotEmpty()
    }

    override fun shouldTerminate(): Boolean {
        return false
    }

    override fun execute() {
        val ids = myContexts
            .filter { it.key != mySelected && !it.value.hasAnyChangeOpened() }
            .map { it.key }

        if (ids.isNotEmpty()) {
            ids.forEach {
                val context = myContexts.remove(it)
                if (null !== context) {
                    context.messageBusConnection.disconnect()
                }
            }
            myLogger.info("Clear inactive contexts Id: ${ids.joinToString(", ")}")
        }
    }

    override fun terminate() {
    }
}
