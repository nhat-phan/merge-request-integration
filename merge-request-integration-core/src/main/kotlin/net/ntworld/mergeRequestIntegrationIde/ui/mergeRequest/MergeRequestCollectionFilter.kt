package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.icons.AllIcons
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.popup.AbstractPopup
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.provider.github.Github
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GithubConnectionsConfigurableBase
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabConnectionsConfigurableBase
import net.ntworld.mergeRequestIntegrationIde.ui.panel.MergeRequestFilterPropertiesPanel
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MergeRequestCollectionFilter(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val providerData: ProviderData
) : MergeRequestCollectionFilterUI {
    override val eventDispatcher = EventDispatcher.create(MergeRequestCollectionFilterEventListener::class.java)
    private var myOrdering = MergeRequestOrdering.RECENTLY_UPDATED

    private val myFilterPropertiesChanged: (() -> Unit) = {
        eventDispatcher.multicaster.filterChanged(myAdvanceFilterButton.buildFilter(mySearchField.text))
    }
    private val mySearchField = MySearchTextField(this)
    private val myMainActionGroup = DefaultActionGroup()
    private val myToolbar = ActionManager.getInstance().createActionToolbar(
        "${MergeRequestCollectionFilter::class.java.canonicalName}${providerData.id}/toolbar",
        myMainActionGroup,
        true
    )
    private val myAdvanceFilterButton: AdvanceFilterButton = AdvanceFilterButton(
        applicationService, ideaProject, providerData, myToolbar.component, myFilterPropertiesChanged
    )

    private val myPanel by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left]0[left, fill]push[right]", "center"))
        myMainActionGroup.add(myAdvanceFilterButton)

        val rightCornerActionGroup = DefaultActionGroup()
        rightCornerActionGroup.add(myOrderByOldestAction)
        rightCornerActionGroup.add(myOrderByNewestAction)
        rightCornerActionGroup.add(myOrderByRecentUpdatedAction)

        val rightCornerToolbar = ActionManager.getInstance().createActionToolbar(
            "${MergeRequestCollectionFilter::class.java.canonicalName}${providerData.id}/toolbar-right",
            rightCornerActionGroup,
            true
        )

        val textFilter = Wrapper(mySearchField)
        textFilter.setVerticalSizeReferent(myToolbar.component)
        textFilter.border = JBUI.Borders.emptyLeft(5)

        panel.add(textFilter)
        panel.add(myToolbar.component)
        panel.add(rightCornerToolbar.component)
        panel
    }

    private val myOrderByRecentUpdatedAction = OrderButton(
        this, MergeRequestOrdering.RECENTLY_UPDATED,
        "Order by recent updated", "Order by recent updated", AllIcons.Plugins.Updated
    )
    private val myOrderByNewestAction = OrderButton(
        this, MergeRequestOrdering.NEWEST,
        "Order by newest", "Order by newest", AllIcons.General.Modified
    )
    private val myOrderByOldestAction = OrderButton(
        this, MergeRequestOrdering.OLDEST,
        "Order by oldest", "Order by oldest", AllIcons.Vcs.History
    )
    private val myKeyListener = object : KeyListener {
        override fun keyTyped(e: KeyEvent?) {
        }

        override fun keyPressed(e: KeyEvent?) {
        }

        override fun keyReleased(e: KeyEvent?) {
            if (null === e || (e.keyCode != 10 && e.keyCode != 13)) {
                return
            }
            val filter = myAdvanceFilterButton.buildFilter(mySearchField.text)
            eventDispatcher.multicaster.filterChanged(filter)
            saveFilterAndOrdering(filter, null)
            mySearchField.addCurrentTextToHistory()
        }
    }

    init {
        mySearchField.addKeyboardListener(myKeyListener)
        val pair = applicationService.getProjectService(ideaProject).findFiltersByProviderId(
            getProviderIdForSavingFilter()
        )
        myOrdering = pair.second
        mySearchField.text = pair.first.search
        myAdvanceFilterButton.setPreselectedValues(pair.first)
    }

    private fun saveFilterAndOrdering(filter: GetMergeRequestFilter?, ordering: MergeRequestOrdering?) {
        applicationService.getProjectService(
            ideaProject
        ).saveFiltersOfProvider(
            getProviderIdForSavingFilter(),
            if (null === filter) myAdvanceFilterButton.buildFilter(mySearchField.text) else filter,
            if (null === ordering) myOrdering else ordering
        )
    }

    private fun getProviderIdForSavingFilter() : String {
        if (providerData.info.id == Gitlab.id) {
            return GitlabConnectionsConfigurableBase.findIdFromName(providerData.name)
        }
        if (providerData.info.id == Github.id) {
            return GithubConnectionsConfigurableBase.findIdFromName(providerData.name)
        }
        return providerData.name
    }

    override fun createComponent(): JComponent {
        return myPanel
    }

    private class OrderButton(
        private val self: MergeRequestCollectionFilter,
        private val order: MergeRequestOrdering,
        text: String,
        desc: String,
        icon: Icon
    ) : ToggleAction(text, desc, icon) {
        override fun isSelected(e: AnActionEvent): Boolean {
            return self.myOrdering == order
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            if (state) {
                self.myOrdering = order
                self.eventDispatcher.multicaster.orderChanged(order)
                self.saveFilterAndOrdering(null, order)
            }
        }
    }

    private class MySearchTextField(private val self: MergeRequestCollectionFilter): SearchTextField() {
        init {
            setHistoryPropertyName("${MergeRequestCollectionFilter::class.java.canonicalName}:${self.providerData.id}")
        }

        override fun onFieldCleared() {
            val filter = self.myAdvanceFilterButton.buildFilter(self.mySearchField.text)
            self.eventDispatcher.multicaster.filterChanged(filter)
            self.saveFilterAndOrdering(filter, null)
        }
    }

    private class AdvanceFilterButton(
        private val applicationService: ApplicationService,
        private val ideaProject: IdeaProject,
        private val providerData: ProviderData,
        private val preferableFocusComponent: JComponent,
        private val onChanged: (() -> Unit)
    ) : AnAction(null, null, AllIcons.Actions.Properties) {
        private var myIsReady = false
        private var myPreselectedValue: GetMergeRequestFilter? = null
        private val myFilterPropertiesReady: (() -> Unit) = {
            myIsReady = true
            val preselected = myPreselectedValue
            if (null !== preselected) {
                myFilterPropertiesPanel.setPreselectedValues(preselected)
                myPreselectedValue = null
            }
        }
        private val myFilterPropertiesPanel = MergeRequestFilterPropertiesPanel(
            applicationService, ideaProject, providerData, onChanged, myFilterPropertiesReady
        )

        override fun actionPerformed(e: AnActionEvent) {
            val component = myFilterPropertiesPanel.createComponent()
            val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(
                    component,
                    preferableFocusComponent
                )
                .setResizable(true)
                .setMovable(false)
                .setRequestFocus(true)
                .createPopup()

            HelpTooltip.setMasterPopup(preferableFocusComponent, popup)
            val point = findPopupPoint(preferableFocusComponent)
            (popup as AbstractPopup).show(preferableFocusComponent, point.x, point.y, false)
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            e.presentation.isEnabled = myIsReady
        }

        private fun findPopupPoint(reference: JComponent): Point {
            val visibleBounds = reference.visibleRect
            val containerScreenPoint = visibleBounds.location
            SwingUtilities.convertPointToScreen(containerScreenPoint, reference)
            visibleBounds.location = containerScreenPoint
            return Point(
                visibleBounds.x,
                visibleBounds.y + reference.height
            )
        }

        fun buildFilter(search: String) = myFilterPropertiesPanel.buildFilter(search)

        fun setPreselectedValues(value: GetMergeRequestFilter) {
            if (myIsReady) {
                myFilterPropertiesPanel.setPreselectedValues(value)
                myPreselectedValue = null
            } else {
                myPreselectedValue = value
            }
        }
    }
}