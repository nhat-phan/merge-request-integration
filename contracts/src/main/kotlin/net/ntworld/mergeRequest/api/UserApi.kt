package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequest.UserInfo

interface UserApi {
    fun find(id: String): UserInfo

    fun me(): User
}