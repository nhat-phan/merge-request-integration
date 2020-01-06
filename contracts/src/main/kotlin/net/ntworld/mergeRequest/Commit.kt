package net.ntworld.mergeRequest

interface Commit {
    val id: String

    val message: String

    val authorName: String

    val authorEmail: String

    val createdAt: String

    companion object
}