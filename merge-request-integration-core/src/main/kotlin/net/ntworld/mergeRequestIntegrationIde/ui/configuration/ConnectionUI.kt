package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface ConnectionUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun initialize(name: String, credentials: ApiCredentials, shared: Boolean, repository: String)

    fun setName(name: String)

    fun onConnectionTested(name: String, connection: ApiConnection, shared: Boolean)

    fun onConnectionError(name: String, connection: ApiConnection, shared: Boolean, exception: Exception)

    fun onCredentialsVerified(name: String, credentials: ApiCredentials, repository: String, project: Project)

    fun onCredentialsInvalid(name: String, credentials: ApiCredentials, repository: String)

    interface Listener : EventListener {
        fun test(connectionUI: ConnectionUI, name: String, connection: ApiConnection, shared: Boolean)

        fun verify(connectionUI: ConnectionUI, name: String, credentials: ApiCredentials, repository: String)

        fun delete(connectionUI: ConnectionUI, name: String)

        fun update(
            connectionUI: ConnectionUI,
            name: String,
            credentials: ApiCredentials,
            shared: Boolean,
            repository: String
        )

        fun changeName(connectionUI: ConnectionUI, oldName: String, newName: String)
    }
}