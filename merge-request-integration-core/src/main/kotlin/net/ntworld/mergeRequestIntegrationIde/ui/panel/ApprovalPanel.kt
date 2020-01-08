package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import java.util.*
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel


class ApprovalPanel : Component {
    var myWholePanel: JPanel? = null
    var myApproversWrapper: JPanel? = null
    var myApproveBtn: JButton? = null
    private var canApprove = false
    private var canUnapprove = false
    private val dispatcher = EventDispatcher.create(Listener::class.java)

    init {
        myApproversWrapper!!.layout = BoxLayout(myApproversWrapper!!, BoxLayout.Y_AXIS)
        myApproveBtn!!.addActionListener {
            if (canApprove) {
                dispatcher.multicaster.onApproveClicked()
            }
            if (canUnapprove) {
                dispatcher.multicaster.onUnapproveClicked()
            }
        }
    }

    fun hide() {
        canApprove = false
        canUnapprove = false
        myWholePanel!!.isVisible = false
    }

    fun setApproval(approval: Approval) {
        canApprove = false
        canUnapprove = false
        myApproveBtn!!.isVisible = approval.canApprove || approval.hasApproved
        if (approval.hasApproved) {
            myApproveBtn!!.text = "Revoke approval"
            canUnapprove = true
        } else {
            myApproveBtn!!.text = "Approve"
            canApprove = true
        }

        myApproversWrapper!!.removeAll()
        val approved = mutableListOf<String>()
        for (approver in approval.approvedBy) {
            myApproversWrapper!!.add(UserInfoItemPanel(approver, Icons.Approved).createComponent())
            approved.add(approver.id)
        }
        for (approver in approval.approvers) {
            if (approved.contains(approver.id)) {
                continue
            }
            myApproversWrapper!!.add(UserInfoItemPanel(approver, Icons.NoApproval).createComponent())
        }
        myWholePanel!!.isVisible = true
    }

    fun addListener(listener: Listener) {
        dispatcher.addListener(listener)
    }

    override fun createComponent(): JComponent = myWholePanel!!

    interface Listener : EventListener {
        fun onApproveClicked()

        fun onUnapproveClicked()
    }
}