package net.ntworld.mergeRequest

interface MergeRequestInfo {
    val id: String

    val provider: String

    val projectId: String

    val title: String

    val description: String

    val url: String

    val state: MergeRequestState

    val createdAt: DateTime

    val updatedAt: DateTime

    companion object
}