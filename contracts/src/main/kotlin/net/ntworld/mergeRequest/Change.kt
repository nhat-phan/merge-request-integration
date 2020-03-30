package net.ntworld.mergeRequest

interface Change {
    val oldPath: String
    val newPath: String
    val aMode: String
    val bMode: String
    val newFile: Boolean
    val renamedFile: Boolean
    val deletedFile: Boolean

    companion object
}
