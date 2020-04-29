package net.ntworld.mergeRequestIntegrationIde.infrastructure.internal

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContextManager

class ReviewContextManagerImpl(
    private val ideaProject: Project
) : ReviewContextManager {
    private val myLogger = Logger.getInstance(this.javaClass)
    private val myContexts = mutableMapOf<String, ReviewContextImpl>()
    private var mySelected: String? = null
    private var myDoingCodeReviewContext: String? = null
    override val interval: Long = 60000

    private fun keyOf(providerId: String, mergeRequestId: String) = "$providerId:$mergeRequestId"

    override fun initContext(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, selected: Boolean) {
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

    override fun isDoingCodeReview(providerId: String, mergeRequestId: String): Boolean {
        val key = myDoingCodeReviewContext
        return if (null === key) false else keyOf(providerId, mergeRequestId) == key
    }

    override fun findSelectedContext() : ReviewContext? {
        val key = mySelected
        if (null === key) {
            return null
        }
        return myContexts[key]
    }

    override fun findDoingCodeReviewContext(): ReviewContext? {
        val key = myDoingCodeReviewContext
        if (null === key) {
            return null
        }
        return myContexts[key]
    }

    override fun findContext(providerId: String, mergeRequestId: String): ReviewContext? {
        return myContexts[keyOf(providerId, mergeRequestId)]
    }

    override fun setSelected(providerId: String, mergeRequestId: String) {
        mySelected = keyOf(providerId, mergeRequestId)
    }

    override fun clearContextDoingCodeReview() {
        myDoingCodeReviewContext = null
    }

    override fun setContextToDoingCodeReview(providerId: String, mergeRequestId: String) {
        myDoingCodeReviewContext = keyOf(providerId, mergeRequestId)
    }

    override fun updateComments(providerId: String, mergeRequestId: String, comments: List<Comment>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update comments for $key")
            context.comments = comments
        }
    }

    override fun updateCommits(providerId: String, mergeRequestId: String, commits: List<Commit>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update commits for $key")
            context.commits = commits
        }
    }

    override fun updateChanges(providerId: String, mergeRequestId: String, changes: List<Change>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update changes for $key")
            context.changes = changes
        }
    }

    override fun updateReviewingCommits(providerId: String, mergeRequestId: String, commits: List<Commit>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update reviewingCommits for $key")
            context.reviewingCommits = commits
        }
    }

    override fun updateReviewingChanges(providerId: String, mergeRequestId: String, changes: List<Change>) {
        val key = keyOf(providerId, mergeRequestId)
        val context = myContexts[key]
        if (null !== context) {
            myLogger.info("Update reviewingChanges for $key")
            context.reviewingChanges = changes
        }
    }

    override fun updateMergeRequest(providerId: String, mergeRequest: MergeRequest) {
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
            .filter { it.key != mySelected && it.key != myDoingCodeReviewContext && !it.value.hasAnyChangeOpened() }
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