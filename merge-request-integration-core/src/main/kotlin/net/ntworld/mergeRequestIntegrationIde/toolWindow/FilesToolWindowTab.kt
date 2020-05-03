package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.Component

interface FilesToolWindowTab : Component {

    fun setChanges(providerData: ProviderData, changes: List<Change>)

    fun hide()

}