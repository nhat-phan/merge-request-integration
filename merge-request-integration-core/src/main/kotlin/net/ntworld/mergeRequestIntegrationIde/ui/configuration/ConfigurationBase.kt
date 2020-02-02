package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.options.SearchableConfigurable
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings
import javax.swing.JComponent

abstract class ConfigurationBase(
    private val applicationService: ApplicationService
) : SearchableConfigurable {
    private var myInitializedSettings = applicationService.settings
    private var myCurrentSettings = applicationService.settings
    private val mySettingsUI: SettingsUI = SettingsConfiguration()
    private val mySettingsListener = object: SettingsUI.Listener {
        override fun change(settings: ApplicationSettings) {
            myCurrentSettings = settings
        }
    }

    init {
        mySettingsUI.initialize(myInitializedSettings)

        mySettingsUI.dispatcher.addListener(mySettingsListener)
    }

    override fun isModified(): Boolean {
        return myCurrentSettings != myInitializedSettings
    }

    override fun apply() {
        applicationService.updateSettings(myCurrentSettings)
        myInitializedSettings = myCurrentSettings
        ApiProviderManager.updateApiOptions(myCurrentSettings.toApiOptions())
    }

    override fun reset() {
        myCurrentSettings = myInitializedSettings
        mySettingsUI.initialize(myInitializedSettings)
    }

    override fun createComponent(): JComponent? = mySettingsUI.createComponent()
}