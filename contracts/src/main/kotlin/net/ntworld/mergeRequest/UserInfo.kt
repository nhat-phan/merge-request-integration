package net.ntworld.mergeRequest

interface UserInfo {
    val id: String

    val name: String

    val username: String

    val avatarUrl: String

    val url: String

    val status: UserStatus

    companion object
}