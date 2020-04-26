package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.ui.ChangesToolWindowFactoryBase

class ChangesToolWindowFactory : ChangesToolWindowFactoryBase(
    ServiceManager.getService(EnterpriseApplicationServiceProvider::class.java)
)
