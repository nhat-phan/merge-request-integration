package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.comments.DiffExtensionBase

class DiffExtension : DiffExtensionBase(
    ServiceManager.getService(CommunityApplicationService::class.java)
)