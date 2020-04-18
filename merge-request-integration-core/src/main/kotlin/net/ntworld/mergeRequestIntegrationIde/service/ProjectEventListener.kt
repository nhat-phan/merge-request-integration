package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.*
import java.util.*

interface ProjectEventListener : EventListener {

    fun providersClear() {}

    fun providerRegistered(providerData: ProviderData) {}

    fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {}

    fun codeReviewChangesSet(providerData: ProviderData, mergeRequest: MergeRequest, changes: Collection<Change>) {}

    fun codeReviewCommitsSet(providerData: ProviderData, mergeRequest: MergeRequest, commit: Collection<Commit>) {}

    fun codeReviewCommentsSet(providerData: ProviderData, mergeRequest: MergeRequest, comments: Collection<Comment>) {}

    fun stopCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {}

}