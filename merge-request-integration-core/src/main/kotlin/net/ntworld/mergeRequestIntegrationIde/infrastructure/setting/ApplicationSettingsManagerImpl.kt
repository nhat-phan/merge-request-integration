package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting

import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.option.*
import org.jdom.Element

class ApplicationSettingsManagerImpl(
    private val changedInvoker: ((ApplicationSettings, ApplicationSettings) -> Unit)
) : ApplicationSettingsManager {
    private var mySettings: ApplicationSettings = ApplicationSettingsImpl.DEFAULT
    private val myOptionEnableRequestCache = EnableRequestCacheOption()
    private val myOptionSaveMRFilterState = SaveMRFilterStateOption()
    private val myOptionDisplayCommentsInDiffView = DisplayCommentsInDiffViewOption()
    private val myOptionShowAddCommentIconsInDiffViewGutter = ShowAddCommentIconsInDiffViewGutterOption()
    private val myOptionCheckoutTargetBranch = CheckoutTargetBranchOption()
    private val myOptionMaxDiffChangesOpenedAutomatically = MaxDiffChangesOpenedAutomaticallyOption()
    private val myOptionDisplayUpVotesAndDownVotes = DisplayUpVotesAndDownVotesOption()
    private val myOptionDisplayMergeRequestState = DisplayMergeRequestStateOption()
    private val myOptionEnableReworkProcess = EnableReworkProcessOption()
    private val myAllSettingOptions = listOf<SettingOption<*>>(
        myOptionEnableRequestCache,
        myOptionSaveMRFilterState,
        myOptionDisplayCommentsInDiffView,
        myOptionShowAddCommentIconsInDiffViewGutter,
        myOptionCheckoutTargetBranch,
        myOptionMaxDiffChangesOpenedAutomatically,
        myOptionDisplayUpVotesAndDownVotes,
        myOptionDisplayMergeRequestState,
        myOptionEnableReworkProcess
    )

    val settings: ApplicationSettings
        get() = mySettings

    override val enableRequestCache: Boolean
        get() = mySettings.enableRequestCache

    override val saveMRFilterState: Boolean
        get() = mySettings.saveMRFilterState

    override val displayCommentsInDiffView: Boolean
        get() = mySettings.displayCommentsInDiffView

    override val showAddCommentIconsInDiffViewGutter: Boolean
        get() = mySettings.showAddCommentIconsInDiffViewGutter

    override val checkoutTargetBranch: Boolean
        get() = mySettings.checkoutTargetBranch

    override val maxDiffChangesOpenedAutomatically: Int
        get() = mySettings.maxDiffChangesOpenedAutomatically

    override val displayUpVotesAndDownVotes: Boolean
        get() = mySettings.displayUpVotesAndDownVotes

    override val displayMergeRequestState: Boolean
        get() = mySettings.displayMergeRequestState

    override val enableReworkProcess: Boolean
        get() = mySettings.enableReworkProcess

    override fun readFrom(elements: List<Element>): ApplicationSettings {
        var settings = ApplicationSettingsImpl.DEFAULT
        for (item in elements) {
            if (item.name != "Setting") {
                continue
            }

            val nameAttribute = item.getAttribute("name")
            if (null === nameAttribute) {
                continue
            }

            val valueAttribute = item.getAttribute("value")
            if (null === valueAttribute) {
                continue
            }

            for (option in myAllSettingOptions) {
                if (option.name == nameAttribute.value.trim()) {
                    settings = option.readValue(valueAttribute.value, settings)
                }
            }
        }
        mySettings = settings
        return settings
    }

    override fun writeTo(element: Element) {
        writeOption(element, myOptionEnableRequestCache, mySettings.enableRequestCache)
        writeOption(element, myOptionSaveMRFilterState, mySettings.saveMRFilterState)
        writeOption(element, myOptionDisplayCommentsInDiffView, mySettings.displayCommentsInDiffView)
        writeOption(element, myOptionShowAddCommentIconsInDiffViewGutter, mySettings.showAddCommentIconsInDiffViewGutter)
        writeOption(element, myOptionCheckoutTargetBranch, mySettings.checkoutTargetBranch)
        writeOption(element, myOptionMaxDiffChangesOpenedAutomatically, mySettings.maxDiffChangesOpenedAutomatically)
        writeOption(element, myOptionDisplayUpVotesAndDownVotes, mySettings.displayUpVotesAndDownVotes)
        writeOption(element, myOptionDisplayMergeRequestState, mySettings.displayMergeRequestState)
        writeOption(element, myOptionEnableReworkProcess, mySettings.enableReworkProcess)
    }

    override fun update(settings: ApplicationSettings) {
        val oldSettings = mySettings
        mySettings = settings
        changedInvoker.invoke(oldSettings, settings)
    }

    private fun<T> writeOption(root: Element, option: SettingOption<T>, value: T) {
        val item = Element("Setting")
        item.setAttribute("name", option.name)
        item.setAttribute("value", option.writeValue(value))
        root.addContent(item)
    }
}