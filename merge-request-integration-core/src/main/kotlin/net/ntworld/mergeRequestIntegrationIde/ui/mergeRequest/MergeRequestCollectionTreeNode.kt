package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData

class MergeRequestCollectionTreeNode(
    private val providerData: ProviderData,
    ideaProject: IdeaProject,
    var mergeRequestInfo: MergeRequestInfo
): PresentableNodeDescriptor<MergeRequestInfo>(ideaProject, null) {
    override fun update(presentation: PresentationData) {
        val id = providerData.info.formatMergeRequestId(mergeRequestInfo.id)
        presentation.addText("$id · ", SimpleTextAttributes.GRAYED_ATTRIBUTES)

        presentation.addText(mergeRequestInfo.title, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    override fun getElement(): MergeRequestInfo = mergeRequestInfo
}