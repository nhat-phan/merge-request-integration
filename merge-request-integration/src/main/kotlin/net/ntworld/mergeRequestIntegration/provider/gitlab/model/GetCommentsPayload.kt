package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.Serializable

@Serializable
data class GetCommentsPayload(
    val data: Data
) {
    @Serializable
    data class Data(
        val project: Project
    )

    @Serializable
    data class Project(
        val id: String,
        val name: String,
        val mergeRequest: MergeRequest
    )

    @Serializable
    data class MergeRequest(
        val id: String,
        val iid: String,
        val notes: NoteCollection
    )

    @Serializable
    data class NoteCollection(
        val pageInfo: NotesPageInfo,
        val nodes: List<Note>
    )

    @Serializable
    data class NotesPageInfo(
        val endCursor: String,
        val startCursor: String,
        val hasNextPage: Boolean,
        val hasPreviousPage: Boolean
    )

    @Serializable
    data class Note(
        val id: String,
        val body: String,
        val bodyHtml: String,
        val author: User,
        val resolvable: Boolean,
        val resolvedAt: String?,
        val resolvedBy: User?,
        val system: Boolean,
        val createdAt: String,
        val updatedAt: String,
        val position: NotePosition?
    )

    @Serializable
    data class User(
        val name: String,
        val username: String,
        val webUrl: String,
        val avatarUrl: String
    )

    @Serializable
    data class NotePosition(
        val diffRefs: DiffRef,
        val filePath: String,
        val newLine: Int?,
        val oldLine: Int?,
        val newPath: String?,
        val oldPath: String?,
        val positionType: String
    )

    @Serializable
    data class DiffRef(
        val baseSha: String,
        val startSha: String,
        val headSha: String
    )
}