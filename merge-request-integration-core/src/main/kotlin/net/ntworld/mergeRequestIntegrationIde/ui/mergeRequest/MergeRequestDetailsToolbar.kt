package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.popup.AbstractPopup
import com.intellij.util.EventDispatcher
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.command.ApproveMergeRequestCommand
import net.ntworld.mergeRequest.command.UnapproveMergeRequestCommand
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.panel.ApprovalPanel
import net.ntworld.mergeRequestIntegrationIde.ui.service.CodeReviewService
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.ui.util.findVisibilityIconAndTextForApproval
import java.awt.Dimension
import java.awt.Point
import javax.swing.JComponent
import javax.swing.SwingUtilities

class MergeRequestDetailsToolbar(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val providerData: ProviderData,
    private val details: MergeRequestDetailsUI
) : MergeRequestDetailsToolbarUI {
    override val dispatcher = EventDispatcher.create(MergeRequestDetailsToolbarUI.Listener::class.java)

    private val myActionGroup = DefaultActionGroup()
    private var myMergeRequest: MergeRequest? = null
    private var myMergeRequestInfo: MergeRequestInfo? = null
    private var myComments: List<Comment>? = null
    private val projectService = applicationService.getProjectService(ideaProject)

    private val myRefreshAction = object : AnAction("Refresh", "Refresh merge request info", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            val mergeRequestInfo = myMergeRequestInfo
            if (null !== mergeRequestInfo) {
                dispatcher.multicaster.refreshRequested(mergeRequestInfo)
            }
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = !projectService.isDoingCodeReview()
        }
    }

    private var myUpVotes = -1
    private var myDownVotes = -1
    private val myUpVotesAction = object : AnAction(null, "Up votes", Icons.ThumbsUp) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            e.presentation.text = myUpVotes.toString()
            e.presentation.isVisible = myUpVotes >= 0
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }
    private val myDownVotesAction = object : AnAction(null, "Down votes", Icons.ThumbsDown) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            e.presentation.text = myDownVotes.toString()
            e.presentation.isVisible = myDownVotes >= 0
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }

    private var myPipelines: List<Pipeline> = listOf()
    private val myPipelineAction = object : AnAction(
        null, "Pipeline status · Click to open pipeline in browser", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            val pipelines = myPipelines
            if (pipelines.isEmpty()) {
                return
            }
            BrowserUtil.open(pipelines.first().url)
        }

        override fun update(e: AnActionEvent) {
            if (myPipelines.isEmpty()) {
                e.presentation.isVisible = false
                return
            }

            val status = myPipelines.first().status
            e.presentation.icon = when (status) {
                PipelineStatus.FAILED -> Icons.PipelineFailed
                PipelineStatus.RUNNING -> Icons.PipelineRunning
                PipelineStatus.PARTIAL_FAILED -> Icons.PipelineFailed
                PipelineStatus.SUCCESS -> Icons.PipelineSuccess
                PipelineStatus.UNKNOWN -> Icons.PipelineRunning
            }
            e.presentation.text = when (status) {
                PipelineStatus.FAILED -> "failed"
                PipelineStatus.RUNNING -> "running"
                PipelineStatus.PARTIAL_FAILED -> "partial failed"
                PipelineStatus.SUCCESS -> "passed"
                PipelineStatus.UNKNOWN -> ""
            }

            e.presentation.isVisible = status != PipelineStatus.UNKNOWN
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }

    private val myApprovalPanel = ApprovalPanel()
    private val myApprovalPanelListener = object : ApprovalPanel.Listener {
        override fun onApproveClicked() {
            val approval = myApproval
            val mr = myMergeRequest
            if (null !== approval && null !== mr) {
                myApprovalPanel.hide()
                applicationService.infrastructure.commandBus() process ApproveMergeRequestCommand.make(
                    providerId = providerData.id,
                    mergeRequestId = mr.id,
                    sha = mr.diffReference!!.headHash
                )
                details.setMergeRequestInfo(mr)
            }
        }

        override fun onUnapproveClicked() {
            val approval = myApproval
            val mr = myMergeRequest
            if (null !== approval && null !== mr) {
                myApprovalPanel.hide()
                applicationService.infrastructure.commandBus() process UnapproveMergeRequestCommand.make(
                    providerId = providerData.id,
                    mergeRequestId = mr.id
                )
                details.setMergeRequestInfo(mr)
            }
        }
    }

    private var myApproval: Approval? = null
    private val myApprovalAction = object : AnAction(null, "Approval", null) {
        override fun actionPerformed(e: AnActionEvent) {
            if (!myApprovalPanel.shouldDisplayApprovalPanel()) {
                return
            }
            val reference = createComponent() // toolbar component
            val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(
                    myApprovalPanel.createComponent(),
                    reference
                )
                .setResizable(true)
                .setMovable(false)
                .setRequestFocus(true)
                .createPopup()

            HelpTooltip.setMasterPopup(reference, popup)
            val point = findPopupPoint(reference, popup.content.preferredSize)
            (popup as AbstractPopup).show(reference, point.x, point.y, false)
        }

        private fun findPopupPoint(reference: JComponent, popup: Dimension): Point {
            val visibleBounds = reference.visibleRect
            val containerScreenPoint = visibleBounds.location
            SwingUtilities.convertPointToScreen(containerScreenPoint, reference)
            visibleBounds.location = containerScreenPoint
            return Point(
                visibleBounds.x + reference.width - popup.width,
                visibleBounds.y + reference.height
            )
        }

        override fun update(e: AnActionEvent) {
            val approval = myApproval
            if (null === approval) {
                e.presentation.isVisible = false
                return
            }

            myApprovalPanel.hide()
            myApprovalPanel.setApproval(approval)
            val triple = approval.findVisibilityIconAndTextForApproval()
            e.presentation.isVisible = triple.first
            e.presentation.icon = triple.second
            e.presentation.text = triple.third
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }

    private var myUrl: String = ""
    private val myOpenUrlAction =
        object : AnAction("Open merge request in browser", "Open merge request in browser", Icons.ExternalLink) {
            override fun actionPerformed(e: AnActionEvent) {
                BrowserUtil.open(myUrl)
            }

            override fun update(e: AnActionEvent) {
                e.presentation.isVisible = myUrl.isNotEmpty()
            }
        }

    private var myState: MergeRequestState = MergeRequestState.ALL
    private val myStateAction = object : AnAction(null, "Merge request state", null) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            when (myState) {
                MergeRequestState.ALL -> {
                    e.presentation.isVisible = false
                }
                MergeRequestState.OPENED -> {
                    e.presentation.icon = Icons.StateOpened
                    e.presentation.text = "opened"
                    e.presentation.isVisible = true
                }
                MergeRequestState.CLOSED -> {
                    e.presentation.icon = Icons.StateClosed
                    e.presentation.text = "closed"
                    e.presentation.isVisible = true
                }
                MergeRequestState.MERGED -> {
                    e.presentation.icon = Icons.StateMerged
                    e.presentation.text = "merged"
                    e.presentation.isVisible = true
                }
            }
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }

    private var myCommits: List<Commit> = listOf()
    private var myReviewCommits: List<Commit> = listOf()
    private val myCodeReviewAction = object : ToggleAction(null, null, null) {
        override fun isSelected(e: AnActionEvent): Boolean {
            return projectService.isDoingCodeReview()
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            val mr = myMergeRequest
            if (null === mr) {
                return
            }
            if (state) {
                val comments = myComments
                if (null !== comments) {
                    projectService.setCodeReviewComments(providerData, mr, comments)
                }
                CodeReviewService.start(applicationService, ideaProject, providerData, mr, myReviewCommits)
            } else {
                CodeReviewService.stop(applicationService, ideaProject, providerData, mr)
            }
        }

        override fun update(e: AnActionEvent) {
            val mr = myMergeRequest
            if (null === mr || myReviewCommits.isEmpty()) {
                e.presentation.isVisible = false
                return
            }

            when (myState) {
                MergeRequestState.ALL, MergeRequestState.CLOSED, MergeRequestState.MERGED -> {
                    e.presentation.isVisible = false
                }
                MergeRequestState.OPENED -> {
                    e.presentation.text = getCodeReviewText()
                    e.presentation.description = "Open diff in IDE and review"
                    e.presentation.isVisible = true
                }
                // TODO: Support view diff
                // MergeRequestState.CLOSED, MergeRequestState.MERGED -> {
                //     e.presentation.text = "View Diff"
                //     e.presentation.isVisible = true
                // }
            }

            val isDoingCodeReview = projectService.isDoingCodeReview()
            if (isDoingCodeReview) {
                val isReviewing = projectService.isReviewing(providerData, mr)
                e.presentation.text = "Stop Reviewing"
                e.presentation.description = "End reviewing and show other MRs"
                e.presentation.isEnabled = isReviewing
            } else {
                e.presentation.isEnabled = true
            }
        }

        private fun getCodeReviewText(): String {
            if (myCommits.size == myReviewCommits.size) {
                return "Code Review"
            }
            return "Code Review ${myReviewCommits.size}/${myCommits.size} commits"
        }

        override fun displayTextInToolbar(): Boolean = true
    }

    private val myToolbar by lazy {
        myActionGroup.add(myRefreshAction)
        myActionGroup.addSeparator()
        myActionGroup.add(myUpVotesAction)
        myActionGroup.add(myDownVotesAction)
        myActionGroup.add(myStateAction)
        myActionGroup.add(myPipelineAction)
        myActionGroup.add(myApprovalAction)
        myActionGroup.addSeparator()
        myActionGroup.add(myOpenUrlAction)
        myActionGroup.addSeparator()
        myActionGroup.add(myCodeReviewAction)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "${MergeRequestDetailsToolbar::class.java.canonicalName}/toolbar-right", myActionGroup, true
        )
        toolbar
    }

    init {
        myApprovalPanel.addListener(myApprovalPanelListener)
    }

    override fun setPipelines(mergeRequestInfo: MergeRequestInfo, pipelines: List<Pipeline>) {
        val currentMR = myMergeRequestInfo
        if (currentMR != null && currentMR.id == mergeRequestInfo.id) {
            myPipelines = pipelines
        }
    }

    override fun setCommits(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
        val currentMR = myMergeRequestInfo
        if (currentMR != null && currentMR.id == mergeRequestInfo.id) {
            myCommits = commits
            myReviewCommits = commits
        }
    }

    override fun setCommitsForReviewing(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
        val currentMR = myMergeRequestInfo
        if (currentMR != null && currentMR.id == mergeRequestInfo.id) {
            myReviewCommits = commits
        }
    }

    override fun setComments(mergeRequestInfo: MergeRequestInfo, comments: List<Comment>) {
        val currentMR = myMergeRequest
        if (currentMR != null && currentMR.id == mergeRequestInfo.id) {
            myComments = comments
            if (projectService.isDoingCodeReview()) {
                projectService.setCodeReviewComments(providerData, currentMR, comments)
            }
        }
    }

    override fun setApproval(mergeRequestInfo: MergeRequestInfo, approval: Approval) {
        val currentMR = myMergeRequestInfo
        if (currentMR != null && currentMR.id == mergeRequestInfo.id) {
            myApproval = approval
        }
    }

    override fun setMergeRequest(mergeRequest: MergeRequest) {
        myMergeRequest = mergeRequest
        myUpVotes = mergeRequest.upVotes
        myDownVotes = mergeRequest.downVotes
        myState = mergeRequest.state
        myUrl = mergeRequest.url
        myToolbar.component.isVisible = true
    }

    override fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo) {
        myMergeRequestInfo = mergeRequestInfo
        myPipelines = listOf()
        myCommits = listOf()
        myReviewCommits = listOf()
        myApproval = null
        myToolbar.component.isVisible = false
    }

    override fun createComponent(): JComponent = myToolbar.component
}