package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.diff.DiffExtensionBase

class DiffExtension : DiffExtensionBase(
    ServiceManager.getService(EnterpriseApplicationService::class.java)
)