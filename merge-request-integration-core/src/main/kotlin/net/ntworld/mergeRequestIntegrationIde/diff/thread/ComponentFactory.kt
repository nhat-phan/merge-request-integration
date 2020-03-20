package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData

object ComponentFactory {

    fun makeGroup(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        ideaProject: IdeaProject,
        borderTop: Boolean,
        groupId: String,
        comments: List<Comment>
    ) : GroupComponent {
        return GroupComponentImpl(borderTop, providerData, mergeRequest, ideaProject, groupId, comments)
    }

    fun makeComment(
        groupComponent: GroupComponent,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        comment: Comment,
        indent: Int
    ): CommentComponent {
        return CommentComponentImpl(groupComponent, providerData, mergeRequest, comment, indent)
    }

    fun makeEditor(
        ideaProject: IdeaProject,
        type: EditorComponent.Type,
        indent: Int
    ): EditorComponent {
        return EditorComponentImpl(ideaProject, type, indent)
    }
}