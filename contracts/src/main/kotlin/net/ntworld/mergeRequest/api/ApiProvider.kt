package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequest.UserInfo

interface ApiProvider {
    val info: ProviderInfo

    val credentials: ApiCredentials

    val cache: Cache

    val user: UserApi

    val mergeRequest: MergeRequestApi

    val project: ProjectApi

    val comment: CommentApi

    val commit: CommitApi

    fun setOptions(options: ApiOptions)

    fun initialize(currentUser: UserInfo)
}
