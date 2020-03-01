package net.ntworld.mergeRequest

import net.ntworld.mergeRequest.api.ApiCredentials

interface ProviderData {
    val id: String

    val key: String

    val name: String

    val info: ProviderInfo

    val credentials: ApiCredentials

    val project: Project

    val currentUser: User

    val repository: String

    val status: ProviderStatus

    val hasApprovalFeature: Boolean
        get() = false

    val hasAssigneeFeature: Boolean
        get() = false

    companion object
}
