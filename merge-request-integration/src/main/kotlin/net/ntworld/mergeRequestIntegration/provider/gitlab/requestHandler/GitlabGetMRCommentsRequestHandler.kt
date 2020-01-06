package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.GetCommentsPayload
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRCommentsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetMRCommentsResponse

@Handler
class GitlabGetMRCommentsRequestHandler : RequestHandler<GitlabGetMRCommentsRequest, GitlabGetMRCommentsResponse> {
    override fun handle(request: GitlabGetMRCommentsRequest): GitlabGetMRCommentsResponse = GitlabFuelClient(
        request = request,
        execute = {
            val response = this.callGraphQL(query = query, variables = mapOf(
                "projectPath" to request.projectFullPath,
                "mrIid" to request.mergeRequestInternalId.toString(),
                "endCursor" to request.endCursor
            ))

            GitlabGetMRCommentsResponse(
                error = null,
                payload = this.json.parse(GetCommentsPayload.serializer(), response)
            )
        },
        failed = {
            GitlabGetMRCommentsResponse(error = it, payload = null)
        }
    )

    private val query = """
query GetNotes($${"projectPath"}: ID!, $${"mrIid"}: String, $${"endCursor"}: String){
  project(fullPath: $${"projectPath"}) {
    id,
    name,
    
    mergeRequest(iid: $${"mrIid"}) {
      id
      iid
      notes(after: $${"endCursor"}) {
        pageInfo {
          endCursor
          startCursor
          hasNextPage
          hasPreviousPage
        }
        nodes {
          id
          body
          bodyHtml
          author {
            name
            username
            webUrl
            avatarUrl
          }
          resolvable
          resolvedAt
          resolvedBy {
            name
            username
            webUrl
            avatarUrl
          }
          system
          createdAt
          updatedAt
          position {
            diffRefs {
              baseSha,
              startSha,
              headSha
            }
            filePath
            newLine
            oldLine
            newPath
            oldPath
            positionType
          }
        }
      }
    }
  }
}
"""
}