package net.ntworld.mergeRequest

interface DiffReference {
    val baseHash: String

    val headHash: String

    val startHash: String

    companion object
}
