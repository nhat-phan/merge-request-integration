package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.task.FindApprovalTask
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.provider.ProviderDetailsMRList
import net.ntworld.mergeRequestIntegrationIde.ui.util.findVisibilityIconAndTextForApproval
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MergeRequestItemPanel(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val displayType: ProviderDetailsMRList.ApprovalStatusDisplayType
): Component {
    var myWholePanel: JPanel? = null
    var myWrapper: JPanel? = null
    var myContentWrapperPanel: JPanel? = null
    var myApprovalStatusWrapperPanel: JPanel? = null
    var myTimeWrapper: JPanel? = null
    var myId: JLabel? = null
    var myTitle: JLabel? = null
    var myCreated: JLabel? = null
    var myUpdated: JLabel? = null
    var myUserStatus: JLabel? = null
    var myAllStatuses: JLabel? = null
    private var myUserStatusColor: JBColor = JBColor.RED
    private val myFindApprovalTaskListener = object : FindApprovalTask.Listener {
        override fun dataReceived(mergeRequestInfo: MergeRequestInfo, approval: Approval) {
            val triple = approval.findVisibilityIconAndTextForApproval()
            myAllStatuses!!.isVisible = triple.first
            myAllStatuses!!.icon = triple.second
            myAllStatuses!!.text = triple.third

            if (displayType == ProviderDetailsMRList.ApprovalStatusDisplayType.STATUSES_AND_MINE_APPROVAL) {
                val approved = approval.approvedBy.firstOrNull {
                    it.id == providerData.currentUser.id
                }
                if (null !== approved) {
                    myUserStatus!!.text = "Approved"
                    myUserStatusColor = JBColor.GREEN
                } else {
                    myUserStatus!!.text = "Waiting"
                    myUserStatusColor = JBColor.RED
                }
                myUserStatus!!.foreground = myUserStatusColor
                myUserStatus!!.isVisible = true
            }
            myApprovalStatusWrapperPanel!!.isVisible = true
        }
    }

    init {
        myId!!.text = providerData.info.formatMergeRequestId(mergeRequestInfo.id)
        myTitle!!.text = mergeRequestInfo.title
        val created = DateTimeUtil.toDate(mergeRequestInfo.createdAt)
        myCreated!!.text = "Created ${DateTimeUtil.toPretty(created)}"
        myCreated!!.toolTipText = DateTimeUtil.formatDate(created)

        val updated = DateTimeUtil.toDate(mergeRequestInfo.updatedAt)
        myUpdated!!.text = "updated ${DateTimeUtil.toPretty(updated)}"
        myUpdated!!.toolTipText = DateTimeUtil.formatDate(updated)

        if (displayType != ProviderDetailsMRList.ApprovalStatusDisplayType.NONE && providerData.hasApprovalFeature) {
            fetchApprovalData()
        } else {
            myApprovalStatusWrapperPanel!!.isVisible = false
        }
    }

    private fun fetchApprovalData() {
        val task = FindApprovalTask(
            applicationService = applicationService,
            ideaProject = ideaProject,
            providerData = providerData,
            mergeRequestInfo = mergeRequestInfo,
            listener = myFindApprovalTaskListener
        )
        task.start()
    }

    fun changeStyle(selected: Boolean, hasFocus: Boolean) {
        val backgroundColor = UIUtil.getListBackground(selected, hasFocus)

        myWholePanel!!.background = backgroundColor
        myWrapper!!.background = backgroundColor
        myContentWrapperPanel!!.background = backgroundColor
        myApprovalStatusWrapperPanel!!.background = backgroundColor
        myTimeWrapper!!.background = backgroundColor

        val foregroundColor = UIUtil.getListForeground(selected, hasFocus)
        myTitle!!.foreground = foregroundColor

        val foregroundColorOrGray = if (selected && hasFocus) foregroundColor else JBColor.gray
        myCreated!!.foreground = foregroundColorOrGray
        myUpdated!!.foreground = foregroundColorOrGray

        if (displayType != ProviderDetailsMRList.ApprovalStatusDisplayType.NONE) {
            val foregroundColorOrCurrent = if (selected && hasFocus) foregroundColor else myUserStatusColor
            myUserStatus!!.foreground = foregroundColorOrCurrent
            myAllStatuses!!.foreground = foregroundColor
        }
    }

    override fun createComponent(): JComponent = myWholePanel!!
}