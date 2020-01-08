package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import net.ntworld.mergeRequest.Commit
import javax.swing.JComponent
import javax.swing.JPanel

class MergeRequestCommitsTab : MergeRequestCommitsTabUI {
    override fun clear() {
    }

    override fun setCommits(commits: List<Commit>) {
    }

    override fun createComponent(): JComponent {
        return JPanel()
    }
}