package net.ntworld.mergeRequestIntegrationIde.infrastructure.internal

import com.intellij.diff.chains.DiffRequestProducer
import com.intellij.diff.impl.CacheDiffRequestProcessor
import com.intellij.diff.requests.NoDiffRequest
import com.intellij.diff.util.DiffPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.DiffPreviewUpdateProcessor
import com.intellij.vcs.log.ui.frame.VcsLogChangesBrowser
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

class DiffRequestProcessorImpl(
    project: Project,
    private val change: Change,
    val reviewContext: ReviewContext? = null
) : CacheDiffRequestProcessor.Simple(project, DiffPlaces.DEFAULT), DiffPreviewUpdateProcessor {
    override fun clear() {
        applyRequest(NoDiffRequest.INSTANCE, false, null)
    }

    override fun getFastLoadingTimeMillis(): Int {
        return 10
    }

    override fun refresh(fromModelRefresh: Boolean) {
        updateRequest()
    }

    override fun getCurrentRequestProvider(): DiffRequestProducer? {
        val context = if (null === reviewContext) {
            HashMap()
        } else {
            mutableMapOf<Key<*>, Any>(
                ReviewContext.KEY to reviewContext
            )
        }
        return VcsLogChangesBrowser.createDiffRequestProducer(project!!, change, context, true)
    }
}