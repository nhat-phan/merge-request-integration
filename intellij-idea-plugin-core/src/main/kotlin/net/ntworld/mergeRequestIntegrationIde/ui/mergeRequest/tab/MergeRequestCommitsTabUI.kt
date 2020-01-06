package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestCommitsTabUI : Component {
    fun clear()

    fun setCommits(commits: List<Commit>)
}