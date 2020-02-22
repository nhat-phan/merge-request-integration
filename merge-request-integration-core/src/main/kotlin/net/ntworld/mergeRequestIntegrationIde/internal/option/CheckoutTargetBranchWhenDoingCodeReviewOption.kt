package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

object CheckoutTargetBranchWhenDoingCodeReviewOption : SettingOption<Boolean> {
    override val name: String = "code-review:checkout-target-branch"

    override fun writeValue(value: Boolean): String {
        return if (value) "1" else "0"
    }

    override fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl {
        val value = input.trim() == "1"
        if (currentSettings.checkoutTargetBranchWhenDoingCodeReview != value) {
            return currentSettings.copy(checkoutTargetBranchWhenDoingCodeReview = value)
        }
        return currentSettings
    }
}