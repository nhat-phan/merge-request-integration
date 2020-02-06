package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.ui.editor.AddCommentEditorActionBase

class AddCommentEditorAction : AddCommentEditorActionBase(
    ServiceManager.getService(CommunityApplicationService::class.java)
)