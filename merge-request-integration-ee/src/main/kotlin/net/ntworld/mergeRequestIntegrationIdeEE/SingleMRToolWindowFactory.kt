package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.toolWindow.SingleMRToolWindowFactoryBase

class SingleMRToolWindowFactory : SingleMRToolWindowFactoryBase(
    ServiceManager.getService(EnterpriseApplicationServiceProvider::class.java)
)