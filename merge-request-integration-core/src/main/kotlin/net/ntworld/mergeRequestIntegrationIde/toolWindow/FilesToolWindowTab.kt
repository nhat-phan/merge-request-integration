package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData

interface FilesToolWindowTab: ReworkToolWindowTab {
    val isCodeReviewChanges: Boolean

    fun setChanges(providerData: ProviderData, changes: List<Change>)

    fun hide()
}