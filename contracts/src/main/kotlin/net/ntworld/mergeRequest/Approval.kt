package net.ntworld.mergeRequest

interface Approval {
    val approved: Boolean

    val approvalsRequired: Int

    val approvalsLeft: Int

    val approvers: List<UserInfo>

    val suggestedApprovers: List<UserInfo>

    val approvedBy: List<UserInfo>

    val hasApproved: Boolean

    val canApprove: Boolean

    companion object
}