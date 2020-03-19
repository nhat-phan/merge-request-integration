package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

class MaxDiffChangesOpenedAutomaticallyOption : SettingOption<Int> {
    override val name: String = "code-review:max-diff-change-opened-automatically"

    override fun writeValue(value: Int): String {
        return if (!isValid(value)) DEFAULT_VALUE.toString() else value.toString()
    }

    override fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl {
        val value = parse(input)
        if (currentSettings.maxDiffChangesOpenedAutomatically != value) {
            return currentSettings.copy(maxDiffChangesOpenedAutomatically = value)
        }
        return currentSettings
    }

    companion object {
        const val DEFAULT_VALUE = 10

        fun parse(input: String) : Int {
            val rawValue = input.trim().toIntOrNull()
            return if (null === rawValue || !isValid(rawValue)) DEFAULT_VALUE else rawValue
        }

        private fun isValid(value: Int) = value in 0..1000
    }
}