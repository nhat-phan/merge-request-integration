package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderInfo

object Gitlab : ProviderInfo {

    override val id: String = "gitlab"

    override val name: String = "GitLab"

    override val iconPath: String = "/icons/gitlab.svg"

    override val icon2xPath: String = "/icons/gitlab@2x.svg"

    override val icon3xPath: String = "/icons/gitlab@3x.svg"

    override val icon4xPath: String = "/icons/gitlab@4x.svg"

    override fun createCommentUrl(mergeRequestUrl: String, comment: Comment): String {
        return "$mergeRequestUrl#note_${comment.id}"
    }

    override fun formatMergeRequestId(mergeRequestId: String): String {
        return "!$mergeRequestId"
    }
}