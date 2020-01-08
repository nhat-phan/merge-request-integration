package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationServiceBase

@State(name = "MergeRequestIntegrationApplicationLevel", storages = [(Storage("merge-request-integration.xml"))])
class ApplicationServiceImpl: ApplicationServiceBase()