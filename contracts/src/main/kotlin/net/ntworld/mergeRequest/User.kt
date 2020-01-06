package net.ntworld.mergeRequest

interface User : UserInfo {
    val email: String

    val createdAt: DateTime

    companion object
}
