package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface GroupComponent : Component, Disposable {
    val id: String

    val comments: List<Comment>

}