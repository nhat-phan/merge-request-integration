package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequestIntegrationIde.Component

interface FilesToolWindowTab : Component {

    fun setChanges(changes: List<Change>)

    fun hide()

}