package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData

object ComponentFactory {

    fun makeGroup(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        ideaProject: IdeaProject,
        borderTop: Boolean,
        groupId: String,
        comments: List<Comment>
    ) : GroupComponent {
        return GroupComponentImpl(borderTop, providerData, mergeRequestInfo, ideaProject, groupId, comments)
    }

    fun makeComment(
        groupComponent: GroupComponent,
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        comment: Comment,
        indent: Int
    ): CommentComponent {
        return CommentComponentImpl(groupComponent, providerData, mergeRequestInfo, comment, indent)
    }

    fun makeEditor(
        ideaProject: IdeaProject,
        type: EditorComponent.Type,
        indent: Int
    ): EditorComponent {
        return EditorComponentImpl(ideaProject, type, indent)
    }
}