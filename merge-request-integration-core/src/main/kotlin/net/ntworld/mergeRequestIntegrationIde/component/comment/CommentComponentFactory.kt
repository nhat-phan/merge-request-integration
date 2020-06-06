package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData

interface CommentComponentFactory {
    fun makeGroup(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        ideaProject: Project,
        borderTop: Boolean,
        groupId: String,
        comments: List<Comment>,
        borderLeftRight: Int,
        showMoveToDialog: Boolean
    ) : GroupComponent

    fun makeComment(
        groupComponent: GroupComponent,
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        comment: Comment,
        indent: Int,
        borderLeftRight: Int,
        showMoveToDialog: Boolean
    ): CommentComponent

    fun makeEditor(
        ideaProject: Project,
        type: EditorComponent.Type,
        indent: Int,
        borderLeftRight: Int,
        showCancelAction: Boolean
    ): EditorComponent
}