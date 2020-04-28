package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.option.MaxDiffChangesOpenedAutomaticallyOption
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettings
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SettingsConfiguration : SettingsUI {
    var myTabbedPane: JTabbedPane? = null
    var myWholePanel: JPanel? = null
    var myPerformancePanel: JPanel? = null
    var myEnableRequestCache: JCheckBox? = null
    var mySaveMRFilterState: JCheckBox? = null

    var myCommentOptionsPanel: JPanel? = null
    var myDisplayCommentsInDiffView: JCheckBox? = null
    var myShowAddCommentIconsInDiffViewGutter: JCheckBox? = null
    var myGroupCommentsByThread: JCheckBox? = null

    var myCodeReviewOptionsPanel: JPanel? = null
    var myCheckoutTargetBranch: JCheckBox? = null

    var myMaxDiffChangesOpenedAutomatically: JTextField? = null

    override val dispatcher = EventDispatcher.create(SettingsUI.Listener::class.java)

    init {
        // myPerformancePanel!!.border = BorderFactory.createTitledBorder("Performance")
        myEnableRequestCache!!.addActionListener { dispatchSettingsUpdated() }
        mySaveMRFilterState!!.addActionListener { dispatchSettingsUpdated() }
        myDisplayCommentsInDiffView!!.addActionListener { dispatchSettingsUpdated() }
        myShowAddCommentIconsInDiffViewGutter!!.addActionListener { dispatchSettingsUpdated() }

        // TODO: remove me when implement the feature
        myGroupCommentsByThread!!.addActionListener { dispatchSettingsUpdated() }
        myGroupCommentsByThread!!.isVisible = false

        myCheckoutTargetBranch!!.addActionListener { dispatchSettingsUpdated() }
        myMaxDiffChangesOpenedAutomatically!!.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
                dispatchSettingsUpdated()
            }

            override fun insertUpdate(e: DocumentEvent?) = changedUpdate(e)
            override fun removeUpdate(e: DocumentEvent?) = changedUpdate(e)
        })
    }

    private fun dispatchSettingsUpdated() {
        val settings =
            ApplicationSettingsImpl(
                enableRequestCache = myEnableRequestCache!!.isSelected,
                saveMRFilterState = mySaveMRFilterState!!.isSelected,
                groupCommentsByThread = myGroupCommentsByThread!!.isSelected,
                displayCommentsInDiffView = myDisplayCommentsInDiffView!!.isSelected,
                showAddCommentIconsInDiffViewGutter = myShowAddCommentIconsInDiffViewGutter!!.isSelected,
                checkoutTargetBranch = myCheckoutTargetBranch!!.isSelected,
                maxDiffChangesOpenedAutomatically = MaxDiffChangesOpenedAutomaticallyOption.parse(
                    myMaxDiffChangesOpenedAutomatically!!.text
                )
            )
        dispatcher.multicaster.change(settings)
    }

    override fun initialize(settings: ApplicationSettings) {
        myEnableRequestCache!!.isSelected = settings.enableRequestCache
        mySaveMRFilterState!!.isSelected = settings.saveMRFilterState
        myGroupCommentsByThread!!.isSelected = settings.groupCommentsByThread
        myDisplayCommentsInDiffView!!.isSelected = settings.displayCommentsInDiffView
        myShowAddCommentIconsInDiffViewGutter!!.isSelected = settings.showAddCommentIconsInDiffViewGutter
        myCheckoutTargetBranch!!.isSelected = settings.checkoutTargetBranch
        myMaxDiffChangesOpenedAutomatically!!.text = settings.maxDiffChangesOpenedAutomatically.toString()
    }

    override fun createComponent(): JComponent = myWholePanel!!
}