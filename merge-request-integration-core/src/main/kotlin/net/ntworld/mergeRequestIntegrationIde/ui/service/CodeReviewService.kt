package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.notification.NotificationType
import com.intellij.openapi.wm.ToolWindowManager
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import kotlin.Exception
import com.intellij.openapi.project.Project as IdeaProject

object CodeReviewService {
    var checkedOut = false

    fun start(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        checkedOut = false
        val projectService = applicationServiceProvider.findProjectServiceProvider(ideaProject)
        projectService.setCodeReviewCommits(providerData, mergeRequest, commits)
        projectService.dispatcher.multicaster.startCodeReview(providerData, mergeRequest)
        val toolWindow = ToolWindowManager.getInstance(ideaProject).getToolWindow(
            applicationServiceProvider.getChangesToolWindowId()
        )
        if (null !== toolWindow) {
            toolWindow.show(null)
        }
        checkout(applicationServiceProvider, ideaProject, providerData, mergeRequest, commits)
    }

    fun stop(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest
    ) {
        applicationServiceProvider.findProjectServiceProvider(ideaProject).dispatcher.multicaster.stopCodeReview(
            providerData, mergeRequest
        )
        val toolWindow = ToolWindowManager.getInstance(ideaProject).getToolWindow(
            applicationServiceProvider.getChangesToolWindowId()
        )
        if (null !== toolWindow) {
            toolWindow.hide(null)
        }
        if (checkedOut) {
            CheckoutService.stop(ideaProject, providerData)
            DisplayChangesService.stop(ideaProject, providerData, mergeRequest)
        }
    }

    private fun checkout(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        if (!applicationServiceProvider.settingsManager.checkoutTargetBranch) {
            return checkoutSuccess(applicationServiceProvider, ideaProject, providerData, mergeRequest, commits)
        }

        CheckoutService.start(ideaProject, providerData, mergeRequest, object : CheckoutService.Listener {
            override fun onError(exception: Exception) {
                applicationServiceProvider.findProjectServiceProvider(ideaProject).notify(
                    "Cannot checkout branch ${mergeRequest.sourceBranch}\n\nPlease do git checkout manually before click Code Review",
                    NotificationType.ERROR
                )
                this@CodeReviewService.stop(applicationServiceProvider, ideaProject, providerData, mergeRequest)
            }

            override fun onSuccess() {
                checkoutSuccess(applicationServiceProvider, ideaProject, providerData, mergeRequest, commits)
            }
        })
    }

    private fun checkoutSuccess(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        checkedOut = true
        EditorStateService.start(ideaProject)
        DisplayChangesService.start(applicationServiceProvider, ideaProject, providerData, mergeRequest, commits)
    }
}