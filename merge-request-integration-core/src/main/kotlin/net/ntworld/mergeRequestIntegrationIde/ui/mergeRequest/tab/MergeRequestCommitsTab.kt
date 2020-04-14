package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.ui.OnePixelSplitter
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectEventListener
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChanges
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChangesUI
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitCollection
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitCollectionUI
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class MergeRequestCommitsTab(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject
) : MergeRequestCommitsTabUI {
    override val dispatcher = EventDispatcher.create(MergeRequestCommitsTabUI.Listener::class.java)

    private val mySplitter = OnePixelSplitter(
        MergeRequestCommitsTab::class.java.canonicalName,
        0.5f
    )
    private val myCollection: CommitCollectionUI = CommitCollection()
    private val myChanges: CommitChangesUI = CommitChanges(applicationService.getProjectService(ideaProject))
    private val myCollectionListener = object : CommitCollectionUI.Listener {
        override fun commitsSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: Collection<Commit>) {
            if (commits.isEmpty()) {
                myChanges.clear()
            } else {
                myChanges.setCommits(providerData, mergeRequestInfo, commits)
            }
            dispatcher.multicaster.commitSelected(providerData, mergeRequestInfo, commits)
        }
    }
    private val myProjectEventListener = object : ProjectEventListener {
        override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myCollection.disable()
            myChanges.disable()
        }

        override fun stopCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myCollection.enable()
            myChanges.enable()
        }
    }

    init {
        mySplitter.firstComponent = myCollection.createComponent()
        mySplitter.secondComponent = myChanges.createComponent()

        myCollection.dispatcher.addListener(myCollectionListener)
        applicationService.getProjectService(ideaProject).dispatcher.addListener(myProjectEventListener)
    }

    override fun clear() {
        myCollection.clear()
    }

    override fun setCommits(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
        myCollection.setCommits(providerData, mergeRequestInfo, commits)
        myChanges.setCommits(providerData, mergeRequestInfo, commits)
    }

    override fun createComponent(): JComponent = mySplitter

}