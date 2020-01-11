package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.ui.OnePixelSplitter
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChanges
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChangesUI
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitCollection
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitCollectionUI
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class MergeRequestCommitsTab(
    private val ideaProject: IdeaProject
) : MergeRequestCommitsTabUI {
    private val mySplitter = OnePixelSplitter(
        MergeRequestCommitsTab::class.java.canonicalName,
        0.5f
    )
    private val myCollection: CommitCollectionUI = CommitCollection()
    private val myChanges: CommitChangesUI = CommitChanges(ideaProject)
    private val myCollectionListener = object : CommitCollectionUI.Listener {
        override fun commitsSelected(providerData: ProviderData, commits: Collection<Commit>) {
            if (commits.isEmpty()) {
                myChanges.clear()
            } else {
                myChanges.setCommits(providerData, commits)
            }
        }
    }

    init {
        mySplitter.firstComponent = myCollection.createComponent()
        mySplitter.secondComponent = myChanges.createComponent()

        myCollection.dispatcher.addListener(myCollectionListener)
    }

    override fun clear() {
        myCollection.clear()
    }

    override fun setCommits(providerData: ProviderData, commits: List<Commit>) {
        myCollection.setCommits(providerData, commits)
        myChanges.setCommits(providerData, commits)
    }

    override fun createComponent(): JComponent = mySplitter

}