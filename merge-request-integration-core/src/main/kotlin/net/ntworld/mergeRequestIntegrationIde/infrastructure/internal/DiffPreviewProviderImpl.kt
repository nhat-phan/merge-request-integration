package net.ntworld.mergeRequestIntegrationIde.infrastructure.internal

import com.intellij.diff.impl.DiffRequestProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.openapi.vcs.changes.DiffPreviewProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

class DiffPreviewProviderImpl(
    private val project: Project,
    val change: Change,
    val reviewContext: ReviewContext? = null
) : DiffPreviewProvider {

    override fun getOwner(): Any {
        return this
    }

    override fun createDiffRequestProcessor(): DiffRequestProcessor {
        return DiffRequestProcessorImpl(project, change, reviewContext)
    }

    override fun getEditorTabName(): String {
        if (null !== reviewContext) {
            return "!${reviewContext.mergeRequestInfo.id}: ${ChangesUtil.getFilePath(change).name}"
        }
        return ChangesUtil.getFilePath(change).name
    }

}