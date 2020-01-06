package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.UserStatus
import net.ntworld.mergeRequest.api.ApiCredentials

object GitlabUtil {
    fun findUserStatus(state: String): UserStatus {
        return when (state) {
            USER_STATE_ACTIVE -> UserStatus.ACTIVE
            USER_STATE_INACTIVE -> UserStatus.INACTIVE
            else -> UserStatus.INACTIVE
        }
    }

    fun hasMergeApprovalFeature(credentials: ApiCredentials): Boolean {
        return credentials.info.contains(GITLAB_HAS_MERGE_APPROVAL_FEATURE)
    }

    fun getMergeApprovalFeatureInfo() = GITLAB_HAS_MERGE_APPROVAL_FEATURE
}