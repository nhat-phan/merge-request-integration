package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequest.UserInfo

data class ApprovalImpl(
    override val approved: Boolean,
    override val approvalsRequired: Int,
    override val approvalsLeft: Int,
    override val suggestedApprovers: List<UserInfo>,
    override val approvers: List<UserInfo>,
    override val approvedBy: List<UserInfo>,
    override val hasApproved: Boolean,
    override val canApprove: Boolean
) : Approval
