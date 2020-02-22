package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.notification.NotificationType
import com.intellij.openapi.wm.ToolWindowManager
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import kotlin.Exception
import com.intellij.openapi.project.Project as IdeaProject

object CodeReviewService {
    var checkedOut = false

    fun start(
        applicationService: ApplicationService,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        checkedOut = false
        val projectService = applicationService.getProjectService(ideaProject)
        projectService.setCodeReviewCommits(providerData, mergeRequest, commits)
        projectService.dispatcher.multicaster.startCodeReview(providerData, mergeRequest)
        val toolWindow = ToolWindowManager.getInstance(ideaProject).getToolWindow(
            applicationService.getChangesToolWindowId()
        )
        if (null !== toolWindow) {
            toolWindow.show(null)
        }
        checkout(applicationService, ideaProject, providerData, mergeRequest, commits)
    }

    fun stop(
        applicationService: ApplicationService,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest
    ) {
        applicationService.getProjectService(ideaProject).dispatcher.multicaster.stopCodeReview(
            providerData, mergeRequest
        )
        val toolWindow = ToolWindowManager.getInstance(ideaProject).getToolWindow(
            applicationService.getChangesToolWindowId()
        )
        if (null !== toolWindow) {
            toolWindow.hide(null)
        }
        if (checkedOut) {
            CheckoutService.stop(ideaProject, providerData, mergeRequest)
            EditorStateService.stop(ideaProject, providerData, mergeRequest)
            DisplayChangesService.stop(ideaProject, providerData, mergeRequest)
        }
    }

    private fun checkout(
        applicationService: ApplicationService,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        if (!applicationService.settings.checkoutTargetBranch) {
            return checkoutSuccess(applicationService, ideaProject, providerData, mergeRequest, commits)
        }

        CheckoutService.start(ideaProject, providerData, mergeRequest, object : CheckoutService.Listener {
            override fun onError(exception: Exception) {
                applicationService.getProjectService(ideaProject).notify(
                    "Cannot checkout branch ${mergeRequest.sourceBranch}\n\nPlease do git checkout manually before click Code Review",
                    NotificationType.ERROR
                )
                this@CodeReviewService.stop(applicationService, ideaProject, providerData, mergeRequest)
            }

            override fun onSuccess() {
                checkoutSuccess(applicationService, ideaProject, providerData, mergeRequest, commits)
            }
        })
    }

    private fun checkoutSuccess(
        applicationService: ApplicationService,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        checkedOut = true
        EditorStateService.start(ideaProject, providerData, mergeRequest)
        DisplayChangesService.start(applicationService, ideaProject, providerData, mergeRequest, commits)
    }
}