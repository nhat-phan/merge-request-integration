package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

internal class CommentComponentFactoryImpl(
    private val projectServiceProvider: ProjectServiceProvider
) : CommentComponentFactory {

    override fun makeGroup(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        ideaProject: IdeaProject,
        borderTop: Boolean,
        groupId: String,
        comments: List<Comment>,
        options: Options
    ) : GroupComponent {
        return GroupComponentImpl(
            this,
            borderTop,
            providerData,
            mergeRequestInfo,
            ideaProject,
            groupId,
            comments,
            options
        )
    }

    override fun makeComment(
        groupComponent: GroupComponent,
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        comment: Comment,
        indent: Int,
        options: Options
    ): CommentComponent {
        return CommentComponentImpl(
            this,
            projectServiceProvider,
            groupComponent,
            providerData,
            mergeRequestInfo,
            comment,
            indent,
            options
        )
    }

    override fun makeEditor(
        ideaProject: IdeaProject,
        type: EditorComponent.Type,
        indent: Int,
        borderLeftRight: Int,
        showCancelAction: Boolean
    ): EditorComponent {
        return EditorComponentImpl(
            ideaProject,
            type,
            indent,
            borderLeftRight,
            showCancelAction
        )
    }
}