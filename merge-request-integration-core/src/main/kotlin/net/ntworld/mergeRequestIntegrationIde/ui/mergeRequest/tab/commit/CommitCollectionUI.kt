package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface CommitCollectionUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun clear()

    fun disable()

    fun enable()

    fun setCommits(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: Collection<Commit>)

    interface Listener : EventListener {
        fun commitsSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: Collection<Commit>)
    }
}