package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifierAdapter
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChanges
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChangesUI
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitCollection
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitCollectionUI
import javax.swing.JComponent

class MergeRequestCommitsTab(
    private val projectServiceProvider: ProjectServiceProvider
) : MergeRequestCommitsTabUI, Disposable {
    override val dispatcher = EventDispatcher.create(MergeRequestCommitsTabUI.Listener::class.java)

    private val mySplitter = OnePixelSplitter(
        MergeRequestCommitsTab::class.java.canonicalName,
        0.5f
    )
    private val myCollection: CommitCollectionUI = CommitCollection()
    private val myChanges: CommitChangesUI = CommitChanges(projectServiceProvider)
    private val myCollectionListener = object : CommitCollectionUI.Listener {
        override fun commitsSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
            if (commits.isEmpty()) {
                myChanges.clear()
            } else {
                myChanges.updateSelectedCommits(providerData, mergeRequestInfo, commits)
            }
            dispatcher.multicaster.commitSelected(providerData, mergeRequestInfo, commits)
        }
    }
    private val myProjectNotifier = object : ProjectNotifierAdapter() {
        override fun startCodeReview(reviewContext: ReviewContext) {
            myCollection.disable()
            myChanges.disable()
        }

        override fun stopCodeReview(reviewContext: ReviewContext) {
            myCollection.enable()
            myChanges.enable()
        }
    }
    private val myConnection = projectServiceProvider.messageBus.connect()

    init {
        mySplitter.firstComponent = myCollection.createComponent()
        mySplitter.secondComponent = myChanges.createComponent()

        myCollection.dispatcher.addListener(myCollectionListener)
        myConnection.subscribe(ProjectNotifier.TOPIC, myProjectNotifier)
        Disposer.register(projectServiceProvider.project, this)
    }

    override fun clear() {
        myCollection.clear()
    }

    override fun setCommits(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
        myCollection.setCommits(providerData, mergeRequestInfo, commits)
        myChanges.setCommits(providerData, mergeRequestInfo, commits)
    }

    override fun createComponent(): JComponent = mySplitter

    override fun dispose() {
        myConnection.disconnect()
    }
}