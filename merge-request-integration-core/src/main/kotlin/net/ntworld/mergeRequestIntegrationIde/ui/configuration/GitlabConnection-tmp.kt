//package net.ntworld.mergeRequestIntegrationIde.ui.configuration
//
//import com.intellij.util.EventDispatcher
//import net.ntworld.mergeRequest.api.ApiConnection
//import net.ntworld.mergeRequest.api.ApiCredentials
//import java.util.*
//import javax.swing.JComponent
//
//class GitlabConnection : ConnectionUI {
//    override val dispatcher = EventDispatcher.create(EventListener::class.java)
//
//    override fun onConnectionTested(id: String, connection: ApiConnection, shared: Boolean) {
//    }
//
//    override fun onConnectionError(id: String, connection: ApiConnection, shared: Boolean) {
//    }
//
//    override fun onCredentialsVerified(id: String, credentials: ApiCredentials, repository: String) {
//    }
//
//    override fun onCredentialsInvalid(id: String, credentials: ApiCredentials, repository: String) {
//    }
//
//    override fun createComponent(): JComponent {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}