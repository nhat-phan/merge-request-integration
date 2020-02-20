package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings
import javax.swing.*

class SettingsConfiguration : SettingsUI {
    var myTabbedPane: JTabbedPane? = null
    var myWholePanel: JPanel? = null
    var myPerformancePanel: JPanel? = null
    var myCommentOptionsPanel: JPanel? = null
    var myEnableRequestCache: JCheckBox? = null
    var myDisplayCommentsInDiffView: JCheckBox? = null
    var myGroupCommentsByThread: JCheckBox? = null
    override val dispatcher = EventDispatcher.create(SettingsUI.Listener::class.java)

    init {
        // myPerformancePanel!!.border = BorderFactory.createTitledBorder("Performance")
        myEnableRequestCache!!.addActionListener { dispatchSettingsUpdated() }
        myGroupCommentsByThread!!.addActionListener { dispatchSettingsUpdated() }
        myDisplayCommentsInDiffView!!.addActionListener { dispatchSettingsUpdated() }
    }

    private fun dispatchSettingsUpdated() {
        val settings = ApplicationSettingsImpl(
            enableRequestCache = myEnableRequestCache!!.isSelected,
            groupCommentsByThread = myGroupCommentsByThread!!.isSelected,
            displayCommentsInDiffView = myDisplayCommentsInDiffView!!.isSelected
        )
        dispatcher.multicaster.change(settings)
    }

    override fun initialize(settings: ApplicationSettings) {
        myEnableRequestCache!!.isSelected = settings.enableRequestCache
        myGroupCommentsByThread!!.isSelected = settings.groupCommentsByThread
        myDisplayCommentsInDiffView!!.isSelected = settings.displayCommentsInDiffView
    }

    override fun createComponent(): JComponent = myWholePanel!!
}