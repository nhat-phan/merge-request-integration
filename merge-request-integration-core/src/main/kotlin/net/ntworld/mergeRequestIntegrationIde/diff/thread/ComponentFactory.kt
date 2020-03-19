package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData

object ComponentFactory {

    fun makeGroup(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        borderTop: Boolean,
        groupId: String,
        comments: List<Comment>
    ) : GroupComponent {
        return GroupComponentImpl(borderTop, providerData, mergeRequest, groupId, comments)
    }

    fun makeComment(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        comment: Comment,
        indent: Int
    ): CommentComponent {
        return CommentComponentImpl(providerData, mergeRequest, comment, indent)
    }

    fun makeEditor(
        ideaProject: IdeaProject,
        indent: Int
    ): EditorComponent {
        return EditorComponentImpl(ideaProject, indent)
    }
}