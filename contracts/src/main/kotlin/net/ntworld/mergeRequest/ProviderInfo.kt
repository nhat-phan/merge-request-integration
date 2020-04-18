package net.ntworld.mergeRequest

interface ProviderInfo {
    val id: String

    val name: String

    val iconPath: String

    val icon2xPath: String

    val icon3xPath: String

    val icon4xPath: String

    fun createCommentUrl(mergeRequestUrl: String, comment: Comment): String

    fun formatMergeRequestId(mergeRequestId: String): String

    companion object
}