package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.ui.ChangesToolWindowFactoryBase

class ChangesToolWindowFactory : ChangesToolWindowFactoryBase(
    ServiceManager.getService(CommunityApplicationService::class.java)
)
