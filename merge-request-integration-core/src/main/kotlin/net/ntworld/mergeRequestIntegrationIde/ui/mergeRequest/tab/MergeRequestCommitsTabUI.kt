package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface MergeRequestCommitsTabUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun clear()

    fun setCommits(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: List<Commit>)

    interface Listener : EventListener {
        fun commitSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: Collection<Commit>)
    }
}