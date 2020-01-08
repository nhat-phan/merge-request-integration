package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderInfo

data class ProviderInfoImpl(
    override val id: String,
    override val name: String,
    override val iconPath: String,
    override val icon2xPath: String,
    override val icon3xPath: String,
    override val icon4xPath: String
): ProviderInfo {
    override fun createCommentUrl(mergeRequestUrl: String, comment: Comment): String {
        return ""
    }
}
