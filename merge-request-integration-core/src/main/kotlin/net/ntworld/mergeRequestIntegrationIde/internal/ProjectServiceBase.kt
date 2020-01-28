package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.EventDispatcher
import net.ntworld.foundation.util.UUIDGenerator
import net.ntworld.mergeRequest.*
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegrationIde.service.*
import net.ntworld.mergeRequestIntegrationIde.task.RegisterProviderTask
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabConnectionsConfigurableBase

open class ProjectServiceBase(private val ideaProject: IdeaProject) : ProjectService, ServiceBase() {
    private var myIsInitialized = false
    private var myCodeReviewManager : CodeReviewManager? = null
    private var myComments: Collection<Comment>? = null
    private var myCommits: Collection<Commit>? = null
    private var myChanges: Collection<Change>? = null

    final override val dispatcher = EventDispatcher.create(ProjectEventListener::class.java)

    override val notification: NotificationGroup = NotificationGroup(
        "Merge Request Integration", NotificationDisplayType.BALLOON, true
    )

    override val commentStore: CommentStore = CommentStoreImpl()
    override val codeReviewManager: CodeReviewManager?
        get() = myCodeReviewManager
    override val codeReviewUtil: CodeReviewUtil = CodeReviewUtilImpl

    private val myProjectEventListener = object : ProjectEventListener {
        override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            val service = CodeReviewManagerImpl(
                ideaProject, providerData, mergeRequest, codeReviewUtil
            )
            val comments = myComments
            if (null !== comments) {
                service.comments = comments
            }

            val commits = myCommits
            if (null !== commits) {
                service.commits = commits
            }

            val changes = myChanges
            if (null !== changes) {
                service.changes = changes
            }

            myCodeReviewManager = service
        }

        override fun stopCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myCodeReviewManager = null
        }
    }

    init {
        dispatcher.addListener(myProjectEventListener)
    }

    override val registeredProviders: List<ProviderData>
        get() {
            if (!myIsInitialized) {
                initialize()
            }
            return ApiProviderManager.providerDataCollection
        }

    override fun supported(): List<ProviderInfo> = supportedProviders

    override fun addProviderConfiguration(
        id: String,
        info: ProviderInfo,
        credentials: ApiCredentials,
        repository: String
    ) {
        myProvidersData[id] = ProviderSettingsImpl(
            id = id,
            info = info,
            credentials = encryptCredentials(info, credentials),
            repository = repository
        )
    }

    override fun removeProviderConfiguration(id: String) {
        myProvidersData.remove(id)
    }

    override fun getProviderConfigurations(): List<ProviderSettings> {
        return myProvidersData.values.map {
            ProviderSettingsImpl(
                id = it.id,
                info = it.info,
                credentials = decryptCredentials(it.info, it.credentials),
                repository = it.repository
            )
        }
    }

    protected open fun initialize() {
        getProviderConfigurations().forEach { register(it) }
    }

    override fun clear() {
        ApiProviderManager.clear()
        dispatcher.multicaster.providersClear()
        myIsInitialized = false
    }

    override fun register(settings: ProviderSettings) {
        var name = ""
        if (settings.info.id == Gitlab.id) {
            name = GitlabConnectionsConfigurableBase.findNameFromId(settings.id)
        }

        val task = RegisterProviderTask(
            ideaProject = ideaProject,
            id = UUIDGenerator.generate(),
            name = name,
            settings = settings,
            listener = object : RegisterProviderTask.Listener {
                override fun onError(exception: Exception) {
                }

                override fun providerRegistered(providerData: ProviderData) {
                    dispatcher.multicaster.providerRegistered(providerData)
                }
            }
        )
        task.start()
        myIsInitialized = true
    }

    override fun isDoingCodeReview(): Boolean = null !== myCodeReviewManager

    override fun isReviewing(providerData: ProviderData, mergeRequest: MergeRequest): Boolean {
        val codeReviewService = myCodeReviewManager
        if (null === codeReviewService) {
            return false
        }
        return codeReviewService.providerData.id == providerData.id &&
            codeReviewService.mergeRequest.id == mergeRequest.id
    }

    override fun setCodeReviewComments(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        comments: Collection<Comment>
    ) {
        myComments = comments
        val codeReviewService = myCodeReviewManager
        if (null !== codeReviewService) {
            codeReviewService.comments = comments
        }
    }

    override fun getCodeReviewComments(): Collection<Comment> {
        val codeReviewService = myCodeReviewManager
        return if (null === codeReviewService) listOf() else codeReviewService.comments
    }

    override fun setCodeReviewCommits(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: Collection<Commit>
    ) {
        myCommits = commits
        val codeReviewService = myCodeReviewManager
        if (null !== codeReviewService) {
            codeReviewService.commits = commits
        }
    }

    override fun getCodeReviewCommits(): Collection<Commit> {
        val codeReviewService = myCodeReviewManager
        return if (null === codeReviewService) listOf() else codeReviewService.commits
    }

    override fun setCodeReviewChanges(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        changes: Collection<Change>
    ) {
        myChanges = changes
        val codeReviewService = myCodeReviewManager
        if (null !== codeReviewService) {
            codeReviewService.changes = changes
        }
        dispatcher.multicaster.codeReviewChangesSet(providerData, mergeRequest, changes)
    }

    override fun getCodeReviewChanges(): Collection<Change> {
        val codeReviewService = myCodeReviewManager
        return if (null === codeReviewService) listOf() else codeReviewService.changes
    }

    override fun notify(message: String) {
        notify(message, NotificationType.INFORMATION)
    }

    override fun notify(message: String, type: NotificationType) {
        val notification = notification.createNotification(message, type)
        notification.notify(ideaProject)
    }
}