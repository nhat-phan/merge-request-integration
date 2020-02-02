package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.ui.MainToolWindowFactoryBase

class MainToolWindowFactory : MainToolWindowFactoryBase(
    ServiceManager.getService(ApplicationServiceImpl::class.java)
)