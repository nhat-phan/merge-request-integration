package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.DateTime
import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequest.UserStatus

data class UserImpl(
    override val id: String,
    override val name: String,
    override val username: String,
    override val avatarUrl: String?,
    override val url: String,
    override val status: UserStatus,
    override val email: String,
    override val createdAt: DateTime
) : User