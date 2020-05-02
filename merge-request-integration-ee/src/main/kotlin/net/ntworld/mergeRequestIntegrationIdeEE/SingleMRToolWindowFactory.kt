package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.toolWindow.AbstractSingleMRToolWindowFactory

class SingleMRToolWindowFactory : AbstractSingleMRToolWindowFactory(
    ServiceManager.getService(EnterpriseApplicationServiceProvider::class.java)
) {
    override fun getToolWindowName(): String = "Merge Request"
}
