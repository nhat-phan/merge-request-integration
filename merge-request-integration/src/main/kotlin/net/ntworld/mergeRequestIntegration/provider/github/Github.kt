package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderInfo

object Github : ProviderInfo {

    override val id: String = "github"

    override val name: String = "Github"

    override val iconPath: String = "/icons/github.svg"

    override val icon2xPath: String = "/icons/github@2x.svg"

    override val icon3xPath: String = "/icons/github@3x.svg"

    override val icon4xPath: String = "/icons/github@4x.svg"

    override fun createCommentUrl(mergeRequestUrl: String, comment: Comment): String {
        return "$mergeRequestUrl#comment_${comment.id}"
    }

}