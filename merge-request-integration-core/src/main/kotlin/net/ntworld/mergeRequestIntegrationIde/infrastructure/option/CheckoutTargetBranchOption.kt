package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

class CheckoutTargetBranchOption : BooleanOption() {
    override val name: String = "code-review:checkout-target-branch"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.checkoutTargetBranch
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(checkoutTargetBranch = value)
    }
}