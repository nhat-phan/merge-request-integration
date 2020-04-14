package net.ntworld.mergeRequestIntegrationIde.ui.util

import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import javax.swing.Icon

fun Approval.findVisibilityIconAndTextForApproval() : Triple<Boolean, Icon?, String?> {
    val required = this.approvalsRequired
    val left = this.approvalsLeft
    val visibility = required > 0
    var icon: Icon? = null
    var text: String? = null
    if (required == 0) {
        return Triple(visibility, icon, text)
    }

    if (left == 0) {
        icon = Icons.Approved
        text = "approved $required/$required"
        return Triple(visibility, icon, text)
    }
    if (left == required) {
        icon = Icons.NoApproval
    } else {
        icon = Icons.RequiredApproval
    }
    text = "approval ${required - left}/$required"
    return Triple(visibility, icon, text)
}
