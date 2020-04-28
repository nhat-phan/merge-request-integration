package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting

import org.jdom.Element

interface ApplicationSettingsManager : ApplicationSettings {
    fun writeTo(element: Element)

    fun readFrom(elements: List<Element>): ApplicationSettings

    fun update(settings: ApplicationSettings)
}
