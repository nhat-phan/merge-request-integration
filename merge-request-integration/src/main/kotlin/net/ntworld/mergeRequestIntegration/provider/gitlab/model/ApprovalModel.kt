package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalModel(
    val approved: Boolean,

    @SerialName("approvals_required")
    val approvalsRequired: Int,

    @SerialName("approvals_left")
    val approvalsLeft: Int,

    @SerialName("suggested_approvers")
    val suggestedApprovers: List<UserInfoModel>,

    val approvers: List<ApproverModel>,

    @SerialName("approved_by")
    val approvedBy: List<ApproverModel>,

    @SerialName("user_has_approved")
    val hasApproved: Boolean,

    @SerialName("user_can_approve")
    val canApprove: Boolean
) {
    companion object {
        val Empty = ApprovalModel(
            approved = false,
            approvalsRequired = 0,
            approvalsLeft = 0,
            suggestedApprovers = listOf(),
            approvers = listOf(),
            approvedBy = listOf(),
            hasApproved = false,
            canApprove = false
        )
    }
}
