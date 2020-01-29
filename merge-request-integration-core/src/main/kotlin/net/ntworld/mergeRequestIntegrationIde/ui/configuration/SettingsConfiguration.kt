package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings
import javax.swing.*

class SettingsConfiguration : SettingsUI {
    var myTabbedPane: JTabbedPane? = null
    var myWholePanel: JPanel? = null
    var myPerformancePanel: JPanel? = null
    var myEnableRequestCache: JCheckBox? = null
    override val dispatcher = EventDispatcher.create(SettingsUI.Listener::class.java)

    init {
        // myPerformancePanel!!.border = BorderFactory.createTitledBorder("Performance")
        myEnableRequestCache!!.addActionListener {
            dispatchSettingsUpdated()
        }
    }

    private fun dispatchSettingsUpdated() {
        val settings = ApplicationSettingsImpl(
            enableRequestCache = myEnableRequestCache!!.isSelected
        )
        dispatcher.multicaster.change(settings)
    }

    override fun initialize(settings: ApplicationSettings) {
        myEnableRequestCache!!.isSelected = settings.enableRequestCache
    }

    override fun createComponent(): JComponent = myWholePanel!!
}