package net.ntworld.mergeRequest

interface Pipeline {
    val id: String

    val hash: String

    val ref: String

    val status: PipelineStatus

    val url: String

    val createdAt: DateTime?

    val updatedAt: DateTime?

    companion object
}
