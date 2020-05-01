package net.ntworld.mergeRequestIntegrationIde.rework

import net.ntworld.mergeRequest.ProviderData

interface ReworkManager {

    fun clearAllBranchWatchers()

    fun createBranchWatcher(providerData: ProviderData)

    fun requestCreateReworkWatcher(providers: List<ProviderData>, branchName: String)

    fun requestCreateReworkWatcher(providerData: ProviderData, branchName: String)

}