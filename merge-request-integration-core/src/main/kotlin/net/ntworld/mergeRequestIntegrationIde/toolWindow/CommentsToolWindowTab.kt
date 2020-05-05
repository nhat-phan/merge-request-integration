package net.ntworld.mergeRequestIntegrationIde.toolWindow

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo

interface CommentsToolWindowTab: ReworkToolWindowTab {
    fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo)

    fun setComments(comments: List<Comment>)

    fun setDisplayResolvedComments(value: Boolean)
}