package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil

data class ProviderDataImpl(
    override val id: String,
    override val key: String,
    override val name: String,
    override val info: ProviderInfo,
    override val credentials: ApiCredentials,
    override val project: Project,
    override val currentUser: User,
    override val repository: String,
    override val status: ProviderStatus
) : ProviderData {
    override val hasApprovalFeature: Boolean
        get() {
            if (this.info.id != Gitlab.id) {
                return true
            }
            if (GitlabUtil.hasMergeApprovalFeature(credentials)) {
                return true
            }
            return false
        }

    override val hasAssigneeFeature: Boolean
        get() {
            if (this.info.id == Gitlab.id) {
                return true
            }
            return false
        }
}
