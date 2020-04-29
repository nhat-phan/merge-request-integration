package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface MergeRequestDetailsToolbarUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun setPipelines(mergeRequestInfo: MergeRequestInfo, pipelines: List<Pipeline>)

    fun setCommits(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>)

    fun setCommitsForReviewing(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>)

    fun setApproval(mergeRequestInfo: MergeRequestInfo, approval: Approval)

    fun setMergeRequest(mergeRequest: MergeRequest)

    fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo)

    interface Listener: EventListener {
        fun refreshRequested(mergeRequestInfo: MergeRequestInfo)
    }
}