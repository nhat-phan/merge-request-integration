package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestDetailsToolbarUI : Component {

    fun setPipelines(mergeRequestInfo: MergeRequestInfo, pipelines: List<Pipeline>)

    fun setCommits(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>)

    fun setComments(mergeRequestInfo: MergeRequestInfo, comments: List<Comment>)

    fun setApproval(mergeRequestInfo: MergeRequestInfo, approval: Approval)

    fun setMergeRequest(mergeRequest: MergeRequest)

    fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo)

}