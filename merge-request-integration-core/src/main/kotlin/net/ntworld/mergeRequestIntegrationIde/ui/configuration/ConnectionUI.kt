package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface ConnectionUI : Component {
    val dispatcher: EventDispatcher<EventListener>

    fun setName(name: String)

    fun onConnectionTested(name: String, connection: ApiConnection, shared: Boolean)

    fun onConnectionError(name: String, connection: ApiConnection, shared: Boolean)

    fun onCredentialsVerified(name: String, credentials: ApiCredentials, repository: String)

    fun onCredentialsInvalid(name: String, credentials: ApiCredentials, repository: String)

    interface Listener : EventListener {
        fun test(name: String, connection: ApiConnection, shared: Boolean)

        fun verify(name: String, credentials: ApiCredentials, repository: String)

        fun connectionDeleted(name: String)

        fun nameChanged(oldName: String, newName: String)
    }
}