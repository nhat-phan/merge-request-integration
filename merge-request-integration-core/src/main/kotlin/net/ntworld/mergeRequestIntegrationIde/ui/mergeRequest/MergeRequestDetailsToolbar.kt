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

    private class MyRefreshAction(private val self: MergeRequestDetailsToolbar) :
        AnAction("Refresh", "Refresh merge request info", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            val mergeRequestInfo = self.myMergeRequestInfo
            if (null !== mergeRequestInfo) {
                self.dispatcher.multicaster.refreshRequested(mergeRequestInfo)
            }
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = !self.projectService.isDoingCodeReview()
        }
    }
    private val myRefreshAction = MyRefreshAction(this)

    private var myUpVotes = -1
    private var myDownVotes = -1
    private class MyUpVotesAction(private val self: MergeRequestDetailsToolbar) :
        AnAction(null, "Up votes", Icons.ThumbsUp) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            e.presentation.text = self.myUpVotes.toString()
            e.presentation.isVisible = self.myUpVotes >= 0
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }
    private val myUpVotesAction = MyUpVotesAction(this)

    private class MyDownVotesAction(private val self: MergeRequestDetailsToolbar) :
        AnAction(null, "Down votes", Icons.ThumbsDown) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            e.presentation.text = self.myDownVotes.toString()
            e.presentation.isVisible = self.myDownVotes >= 0
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }
    private val myDownVotesAction = MyDownVotesAction(this)

    private var myPipelines: List<Pipeline> = listOf()
    private class MyPipelineAction(private val self: MergeRequestDetailsToolbar) : AnAction(
        null, "Pipeline status · Click to open pipeline in browser", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            val pipelines = self.myPipelines
            if (pipelines.isEmpty()) {
                return
            }
            BrowserUtil.open(pipelines.first().url)
        }

        override fun update(e: AnActionEvent) {
            if (self.myPipelines.isEmpty()) {
                e.presentation.isVisible = false
                return
            }

            val status = self.myPipelines.first().status
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
    private val myPipelineAction = MyPipelineAction(this)

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
    private class MyApprovalAction(private val self: MergeRequestDetailsToolbar) : AnAction(null, "Approval", null) {
        override fun actionPerformed(e: AnActionEvent) {
            if (!self.myApprovalPanel.shouldDisplayApprovalPanel()) {
                return
            }
            val reference = self.createComponent() // toolbar component
            val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(
                    self.myApprovalPanel.createComponent(),
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
            val approval = self.myApproval
            if (null === approval) {
                e.presentation.isVisible = false
                return
            }

            self.myApprovalPanel.hide()
            self.myApprovalPanel.setApproval(approval)
            val triple = approval.findVisibilityIconAndTextForApproval()
            e.presentation.isVisible = triple.first
            e.presentation.icon = triple.second
            e.presentation.text = triple.third
        }

        override fun displayTextInToolbar(): Boolean = true
        override fun useSmallerFontForTextInToolbar(): Boolean = true
    }
    private val myApprovalAction = MyApprovalAction(this)

    private var myUrl: String = ""
    private class MyOpenUrlAction(private val self: MergeRequestDetailsToolbar) :
        AnAction("Open merge request in browser", "Open merge request in browser", Icons.ExternalLink) {
        override fun actionPerformed(e: AnActionEvent) {
            BrowserUtil.open(self.myUrl)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = self.myUrl.isNotEmpty()
        }
    }
    private val myOpenUrlAction = MyOpenUrlAction(this)

    private var myState: MergeRequestState = MergeRequestState.ALL
    private class MyStateAction(private val self: MergeRequestDetailsToolbar) :
        AnAction(null, "Merge request state", null) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            when (self.myState) {
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
    private val myStateAction = MyStateAction(this)

    private var myCommits: List<Commit> = listOf()
    private var myReviewCommits: List<Commit> = listOf()
    private class MyCodeReviewAction(private val self: MergeRequestDetailsToolbar) : ToggleAction(null, null, null) {
        override fun isSelected(e: AnActionEvent): Boolean {
            return self.projectService.isDoingCodeReview()
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            val mr = self.myMergeRequest
            if (null === mr) {
                return
            }
            if (state) {
                val comments = self.myComments
                if (null !== comments) {
                    self.projectService.setCodeReviewComments(self.providerData, mr, comments)
                }
                CodeReviewService.start(self.applicationService, self.ideaProject, self.providerData, mr, self.myReviewCommits)
            } else {
                CodeReviewService.stop(self.applicationService, self.ideaProject, self.providerData, mr)
            }
        }

        override fun update(e: AnActionEvent) {
            val mr = self.myMergeRequest
            if (null === mr || self.myReviewCommits.isEmpty()) {
                e.presentation.isVisible = false
                return
            }

            when (self.myState) {
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

            val isDoingCodeReview = self.projectService.isDoingCodeReview()
            if (isDoingCodeReview) {
                val isReviewing = self.projectService.isReviewing(self.providerData, mr)
                e.presentation.text = "Stop Reviewing"
                e.presentation.description = "End reviewing and show other MRs"
                e.presentation.isEnabled = isReviewing
            } else {
                e.presentation.isEnabled = true
            }
        }

        private fun getCodeReviewText(): String {
            if (self.myCommits.size == self.myReviewCommits.size) {
                return "Code Review"
            }
            return "Code Review ${self.myReviewCommits.size}/${self.myCommits.size} commits"
        }

        override fun displayTextInToolbar(): Boolean = true
    }
    private val myCodeReviewAction = MyCodeReviewAction(this)

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