package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.SimpleTextAttributes
import net.ntworld.mergeRequest.MergeRequestInfo

class MergeRequestCollectionTreeNode(
    ideaProject: IdeaProject,
    var mergeRequestInfo: MergeRequestInfo
): PresentableNodeDescriptor<MergeRequestInfo>(ideaProject, null) {
    override fun update(presentation: PresentationData) {
        presentation.addText(mergeRequestInfo.title, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    override fun getElement(): MergeRequestInfo = mergeRequestInfo
}