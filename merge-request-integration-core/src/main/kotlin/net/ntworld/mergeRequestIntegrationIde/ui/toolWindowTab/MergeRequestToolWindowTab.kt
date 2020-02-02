package net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab

import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.content.ContentManager
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.*
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class MergeRequestToolWindowTab(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val contentManager: ContentManager,
    private val providerData: ProviderData
) : Component {
    private val mySplitter by lazy {
        OnePixelSplitter(
            "${MergeRequestToolWindowTab::class.java.canonicalName}:${providerData.info.id}:${providerData.name}",
            0.35f
        )
    }
    private val myCollection: MergeRequestCollectionUI = MergeRequestCollection(applicationService, ideaProject, providerData)
    private val myDetails: MergeRequestDetailsUI = MergeRequestDetails(applicationService, ideaProject, contentManager, providerData)
    private val myCollectionListener = object : MergeRequestCollectionEventListener {
        override fun mergeRequestUnselected() {
            myDetails.hide()
        }

        override fun mergeRequestSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
            myDetails.setMergeRequestInfo(mergeRequestInfo)
        }
    }

    init {
        myCollection.eventDispatcher.addListener(myCollectionListener)
        mySplitter.firstComponent = myCollection.createComponent()
        mySplitter.secondComponent = myDetails.createComponent()
    }

    override fun createComponent(): JComponent = mySplitter

    fun selectMergeRequest(mergeRequestInfo: MergeRequestInfo) {
        myDetails.setMergeRequestInfo(mergeRequestInfo)
    }
}