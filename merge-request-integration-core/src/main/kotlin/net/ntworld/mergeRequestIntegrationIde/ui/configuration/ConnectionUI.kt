package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface ConnectionUI : Component {
    val dispatcher: EventDispatcher<EventListener>

    fun onConnectionTested(id: String, connection: ApiConnection, shared: Boolean)

    fun onConnectionError(id: String, connection: ApiConnection, shared: Boolean)

    fun onCredentialsVerified(id: String, credentials: ApiCredentials, repository: String)

    fun onCredentialsInvalid(id: String, credentials: ApiCredentials, repository: String)

    interface Listener : EventListener {
        fun test(id: String, connection: ApiConnection, shared: Boolean)

        fun verify(id: String, credentials: ApiCredentials, repository: String)

        fun makeNewConnection()

        fun connectionDeleted(id: String)

        fun idChanged(oldId: String, newId: String)
    }
}