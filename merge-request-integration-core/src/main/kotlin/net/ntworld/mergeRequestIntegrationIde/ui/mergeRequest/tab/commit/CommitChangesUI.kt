package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit

import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface CommitChangesUI : Component {

    fun clear()

    fun disable()

    fun enable()

    fun setCommits(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: Collection<Commit>)

}