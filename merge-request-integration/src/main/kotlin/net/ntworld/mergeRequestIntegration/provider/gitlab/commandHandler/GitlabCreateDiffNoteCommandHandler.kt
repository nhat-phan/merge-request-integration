package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import kotlinx.serialization.Serializable
import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.foundation.util.UUIDGenerator
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabCreateDiffNoteCommand

@Handler
class GitlabCreateDiffNoteCommandHandler : CommandHandler<GitlabCreateDiffNoteCommand> {

    override fun handle(command: GitlabCreateDiffNoteCommand) = GitlabFuelClient(
        credentials = command.credentials,
        execute = {
            val clientMutationId = generateClientMutationId()
            val graphqlRequest = CustomGraphqlRequest(
                query = mutation,
                variables = CustomVariables(
                    clientMutationId = clientMutationId,
                    body = attachFooterToBody(command.body, clientMutationId),
                    mrIid = "gid://gitlab/MergeRequest/${command.mergeRequestInternalId}",
                    headSha = command.position.headHash,
                    baseSha = command.position.baseHash,
                    startSha = command.position.startHash,
                    oldPath = command.position.oldPath,
                    oldLine = command.position.oldLine,
                    newPath = command.position.newPath,
                    newLine = command.position.newLine
                )
            )
            this.callGraphQL(json.stringify(CustomGraphqlRequest.serializer(), graphqlRequest))
            Unit
        },
        failed = {
            println(it)
            Unit
        }
    )

    private fun generateClientMutationId(): String {
        return "MRI:${UUIDGenerator.generate()}"
    }

    private fun attachFooterToBody(body: String, clientMutationId: String): String {
        return body
    }

    @Serializable
    data class CustomGraphqlRequest(
        val query: String,
        val variables: CustomVariables
    )

    @Serializable
    data class CustomVariables(
        val clientMutationId: String,
        val body: String,
        val mrIid: String,
        val headSha: String,
        val baseSha: String,
        val startSha: String,
        val oldPath: String?,
        val oldLine: Int?,
        val newPath: String?,
        val newLine: Int?
    )

    private val mutation = """
mutation createComment(
  $${"clientMutationId"}: String, 
  $${"body"}: String!, 
  $${"mrIid"}: ID!,
  $${"headSha"}: String!,
  $${"baseSha"}: String,
  $${"startSha"}: String!,
  $${"oldPath"}: String,
  $${"oldLine"}: Int,
  $${"newPath"}: String,
  $${"newLine"}: Int!
) {
  createDiffNote(input: {
    body: $${"body"}
    noteableId: $${"mrIid"},
    clientMutationId: $${"clientMutationId"},
    position: {
      headSha: $${"headSha"},
      baseSha: $${"baseSha"},
      startSha: $${"startSha"},
      paths: {
        oldPath: $${"oldPath"},
        newPath: $${"newPath"}
      },
      oldLine: $${"oldLine"},
      newLine: $${"newLine"}
    }
  }) {
    clientMutationId
  }
}
"""
}