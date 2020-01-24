package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.options.SearchableConfigurable
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import javax.swing.JComponent

abstract class AbstractConnectionsConfigurable(private val settings: List<ProviderSettings>) : SearchableConfigurable {
    private val data = mutableMapOf<String, MyProviderSettings>()
    private val initializedData: Map<String, MyProviderSettings> by lazy {
        val result = mutableMapOf<String, MyProviderSettings>()
        settings.forEach {
            result[it.id] = MyProviderSettings(
                id = it.id,
                info = it.info,
                credentials = it.credentials,
                repository = it.repository,
                sharable = it.sharable,
                deleted = false
            )
        }
        result
    }

    init {
        initializedData.forEach { (key, value) ->
            data[key] = value
        }
    }

    override fun isModified(): Boolean {
        for (entry in data) {
            if (!initializedData.containsKey(entry.key)) {
                return true
            }
            val initializedItem = initializedData[entry.key]
            if (null === initializedItem) {
                return true
            }
            if (!initializedItem.equals(entry.value)) {
                return true
            }
        }
        return false
    }

    override fun apply() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createComponent(): JComponent? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private data class MyProviderSettings(
        override val id: String,
        override val info: ProviderInfo,
        override val credentials: ApiCredentials,
        override val repository: String,
        override val sharable: Boolean,
        val deleted: Boolean
    ): ProviderSettings
}