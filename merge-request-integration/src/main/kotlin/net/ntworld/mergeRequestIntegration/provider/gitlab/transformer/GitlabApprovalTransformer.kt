package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequestIntegration.internal.ApprovalImpl
import net.ntworld.mergeRequestIntegration.internal.UserInfoImpl
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.ApprovalModel
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.ApproverModel
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.UserInfoModel

object GitlabApprovalTransformer :
    Transformer<ApprovalModel, Approval> {
    override fun transform(input: ApprovalModel): Approval = ApprovalImpl(
        approved = input.approved,
        approvalsRequired = input.approvalsRequired,
        approvalsLeft = input.approvalsLeft,
        suggestedApprovers = input.suggestedApprovers.map { transformUserInfo(it) },
        approvers = input.approvers.map { transformApprover(it) },
        approvedBy = input.approvedBy.map { transformApprover(it) },
        hasApproved = input.hasApproved,
        canApprove = input.canApprove
    )

    private fun transformApprover(input: ApproverModel): UserInfo = transformUserInfo(input.user)

    private fun transformUserInfo(input: UserInfoModel): UserInfo = UserInfoImpl(
        id = input.id.toString(),
        name = input.name,
        username = input.username,
        avatarUrl = input.avatarUrl,
        url = input.webUrl,
        status = GitlabUtil.findUserStatus(input.state)
    )

}