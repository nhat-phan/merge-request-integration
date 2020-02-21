package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabCreateNoteRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabCreateNoteResponse
import java.util.*

@Handler
class GitlabCreateNoteRequestHandler : RequestHandler<GitlabCreateNoteRequest, GitlabCreateNoteResponse> {

    override fun handle(request: GitlabCreateNoteRequest): GitlabCreateNoteResponse = GitlabClient(
        request = request,
        execute = {
            val result = this.discussionsApi.createMergeRequestDiscussion(
                request.credentials.projectId.toInt(),
                request.mergeRequestInternalId,
                request.body,
                Date(),
                null, // This is a bug from the API client, null is okay
                request.position
            )
            if (result.notes.isNotEmpty()) {
                GitlabCreateNoteResponse(error = null, createdCommentId = result.notes.first().id)
            } else {
                GitlabCreateNoteResponse(error = null, createdCommentId = 0)
            }
        },
        failed = {
            GitlabCreateNoteResponse(error = it, createdCommentId = 0)
        }
    )

}