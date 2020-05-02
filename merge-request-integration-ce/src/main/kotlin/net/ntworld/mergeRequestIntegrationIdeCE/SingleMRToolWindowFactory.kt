package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.toolWindow.SingleMRToolWindowFactoryBase

class SingleMRToolWindowFactory : SingleMRToolWindowFactoryBase(
    ServiceManager.getService(CommunityApplicationServiceProvider::class.java)
)