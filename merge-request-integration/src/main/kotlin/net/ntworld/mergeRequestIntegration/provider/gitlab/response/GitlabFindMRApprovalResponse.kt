package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.ApprovalModel

data class GitlabFindMRApprovalResponse(
    override val error: Error?,
    val approval: ApprovalModel
) : Response