package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent
import javax.swing.JPanel

abstract class ConfigurationBase : SearchableConfigurable, Disposable {

    override fun isModified(): Boolean {
        return false
    }

    override fun apply() {
    }

    override fun createComponent(): JComponent? {
        return JPanel()
    }

    override fun dispose() {

    }

}