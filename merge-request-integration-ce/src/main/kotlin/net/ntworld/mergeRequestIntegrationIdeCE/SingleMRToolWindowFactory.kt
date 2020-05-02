package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.toolWindow.AbstractSingleMRToolWindowFactory

class SingleMRToolWindowFactory : AbstractSingleMRToolWindowFactory(
    ServiceManager.getService(CommunityApplicationServiceProvider::class.java)
) {
    override fun getToolWindowName(): String = "Merge Request CE"
}
