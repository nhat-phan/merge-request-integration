package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequest.UserStatus

data class UserInfoImpl(
    override val id: String,
    override val name: String,
    override val username: String,
    override val avatarUrl: String,
    override val url: String,
    override val status: UserStatus
) : UserInfo {
    companion object {
        val None = UserInfoImpl(
            id = "",
            name = "<None>",
            username = "",
            avatarUrl = "",
            url = "",
            status = UserStatus.ACTIVE
        )
    }
}
