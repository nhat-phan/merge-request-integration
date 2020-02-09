package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.User

interface UserApi {
    // @Deprecated("Not used, will be removed", ReplaceWith("Nothing"), DeprecationLevel.HIDDEN)
    // fun find(id: String): UserInfo

    fun me(): User
}