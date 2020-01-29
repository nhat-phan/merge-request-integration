package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface SettingsUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun initialize(settings: ApplicationSettings)

    interface Listener: EventListener {
        fun change(settings: ApplicationSettings)
    }
}