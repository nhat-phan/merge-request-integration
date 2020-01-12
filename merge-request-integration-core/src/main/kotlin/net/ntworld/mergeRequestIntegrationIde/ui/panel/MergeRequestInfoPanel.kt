package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MergeRequestInfoPanel {
    var myWholePanel: JPanel? = null
    var myWrapperPanel: JPanel? = null
    var myBranchesWrapperPanel: JPanel? = null
    var myMergedByWrapperPanel: JPanel? = null
    var myClosedByWrapperPanel: JPanel? = null
    var myAssignedWrapperPanel: JPanel? = null
    var myAuthorWrapperPanel: JPanel? = null
    var myTitle: JLabel? = null
    var myCreatedAt: JLabel? = null
    var myUpdatedAt: JLabel? = null
    var myAuthorFullName: JLabel? = null
    var myAuthorUsername: JLabel? = null
    var myAssigneeFullName: JLabel? = null
    var myAssigneeUsername: JLabel? = null
    var mySourceBranch: JLabel? = null
    var myBranchIcon: JLabel? = null
    var myTargetBranch: JLabel? = null
    var myStatus: JLabel? = null
    var myMergedByFullName: JLabel? = null
    var myMergedByUsername: JLabel? = null
    var myMergedAtText: JLabel? = null
    var myMergedAt: JLabel? = null
    var myClosedByFullName: JLabel? = null
    var myClosedByUsername: JLabel? = null
    var myClosedAtText: JLabel? = null
    var myClosedAt: JLabel? = null

    init {
        myBranchIcon!!.icon = AllIcons.Vcs.Arrow_right
        myTitle!!.text = "-"
        displayTime(null, myCreatedAt!!)
        displayTime(null, myUpdatedAt!!)
        hide()
        setBackground()
    }

    private fun setBackground() {
        myWholePanel!!.background = JBColor.background()
        myWrapperPanel!!.background = JBColor.background()
        myBranchesWrapperPanel!!.background = JBColor.background()
        myMergedByWrapperPanel!!.background = JBColor.background()
        myClosedByWrapperPanel!!.background = JBColor.background()
        myAssignedWrapperPanel!!.background = JBColor.background()
        myAuthorWrapperPanel!!.background = JBColor.background()
    }

    fun setMergeRequestInfo(mr: MergeRequestInfo) {
        myTitle!!.text = mr.title
        displayTime(mr.createdAt, myCreatedAt!!)
        displayTime(mr.updatedAt, myUpdatedAt!!)
        hide()
    }

    fun setMergeRequest(mr: MergeRequest) {
        myTitle!!.text = mr.title
        displayTime(mr.createdAt, myCreatedAt!!)
        displayTime(mr.updatedAt, myUpdatedAt!!)
        displayStatus(mr.state, myStatus!!)
        displayBranches(mr.sourceBranch, mr.targetBranch, mySourceBranch!!, myTargetBranch!!, myBranchIcon!!)
        displayUserInfo(mr.author, myAuthorFullName!!, myAuthorUsername!!)
        displayUserInfo(mr.assignee, myAssigneeFullName!!, myAssigneeUsername!!)
        displayUserInfoWithTime(
            mr.mergedBy,
            mr.mergedAt,
            myMergedByFullName!!,
            myMergedByUsername!!,
            myMergedAtText!!,
            myMergedAt!!
        )
        displayUserInfoWithTime(
            mr.closedBy,
            mr.closedAt,
            myClosedByFullName!!,
            myClosedByUsername!!,
            myClosedAtText!!,
            myClosedAt!!
        )
    }

    fun createComponent(): JComponent = myWholePanel!!

    private fun hide() {
        displayStatus(MergeRequestState.ALL, myStatus!!)
        displayBranches(null, null, mySourceBranch!!, myTargetBranch!!, myBranchIcon!!)
        displayUserInfo(null, myAuthorFullName!!, myAuthorUsername!!)
        displayUserInfo(null, myAssigneeFullName!!, myAssigneeUsername!!)
        displayUserInfoWithTime(
            null,
            null,
            myMergedByFullName!!,
            myMergedByUsername!!,
            myMergedAtText!!,
            myMergedAt!!
        )
        displayUserInfoWithTime(
            null,
            null,
            myClosedByFullName!!,
            myClosedByUsername!!,
            myClosedAtText!!,
            myClosedAt!!
        )
    }

    private fun displayBranches(
        source: String?,
        target: String?,
        sourceLabel: JLabel,
        targetLabel: JLabel,
        icon: JLabel
    ) {
        if (null === source || null === target) {
            sourceLabel.text = "-"
            targetLabel.isVisible = false
            icon.isVisible = false
            return
        }

        sourceLabel.text = source
        targetLabel.text = target
        targetLabel.isVisible = true
        icon.isVisible = true
    }

    private fun displayStatus(state: MergeRequestState, label: JLabel) {
        label.text = when (state) {
            MergeRequestState.ALL -> "-"
            MergeRequestState.OPENED -> "opened"
            MergeRequestState.CLOSED -> "closed"
            MergeRequestState.MERGED -> "merged"
        }
    }

    private fun displayUserInfoWithTime(
        user: UserInfo?,
        datetime: DateTime?,
        fullName: JLabel,
        username: JLabel,
        at: JLabel,
        datetimeLabel: JLabel
    ) {
        displayUserInfo(user, fullName, username)
        displayTime(datetime, datetimeLabel)
        at.isVisible = null !== datetime
    }

    private fun displayTime(datetime: DateTime?, label: JLabel) {
        if (null === datetime) {
            label.text = ""
        } else {
            val date = DateTimeUtil.toDate(datetime)
            label.text = "${DateTimeUtil.formatDate(date)} · ${DateTimeUtil.toPretty(date)}"
        }
    }

    private fun displayUserInfo(user: UserInfo?, fullName: JLabel, username: JLabel) {
        if (null === user) {
            fullName.text = "-"
            username.text = ""
        } else {
            fullName.text = user.name
            username.text = "@${user.username}"
        }
    }
}