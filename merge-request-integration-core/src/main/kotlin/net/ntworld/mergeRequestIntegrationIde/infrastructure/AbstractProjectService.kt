package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.EventDispatcher
import com.intellij.util.messages.MessageBus
import net.ntworld.foundation.util.UUIDGenerator
import net.ntworld.mergeRequest.*
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequest.query.generated.GetMergeRequestFilterImpl
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.provider.MemoryCache
import net.ntworld.mergeRequestIntegration.provider.github.Github
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegration.util.SavedFiltersUtil
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.provider.MergeRequestDataProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ServiceBase
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile.CachedRepositoryFile
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile.LocalRepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.internal.CodeReviewManagerImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.*
import net.ntworld.mergeRequestIntegrationIde.task.RegisterProviderTask
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GithubConnectionsConfigurableBase
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabConnectionsConfigurableBase
import org.jdom.Element

abstract class AbstractProjectService(
    final override val project: IdeaProject
) : ProjectService, ServiceBase() {
    private var myIsInitialized = false
    private var myCodeReviewManager : CodeReviewManager? = null
    private var myComments: Collection<Comment>? = null
    private var myCommits: Collection<Commit>? = null
    private var myChanges: Collection<Change>? = null
    private val myFiltersData: MutableMap<String, Pair<GetMergeRequestFilter, MergeRequestOrdering>> = mutableMapOf()

    final override val messageBus: MessageBus by lazy { project.messageBus }

    final override val dispatcher = EventDispatcher.create(ProjectEventListener::class.java)

    override val notification: NotificationGroup = NotificationGroup(
        "Merge Request Integration", NotificationDisplayType.BALLOON, true
    )

    override val codeReviewManager: CodeReviewManager?
        get() = myCodeReviewManager

    private val myProjectEventListener = object : ProjectEventListener {
        override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            val service = CodeReviewManagerImpl(
                project, providerData, mergeRequest
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
            val reviewContext = findReviewContextWhichDoingCodeReview()
            if (null !== reviewContext) {
                reviewContext.closeAllChanges()
            }

            val codeReviewManager = myCodeReviewManager
            if (null !== codeReviewManager) {
                codeReviewManager.dispose()
            }
            myCodeReviewManager = null
        }
    }

    override val registeredProviders: List<ProviderData>
        get() {
            if (!myIsInitialized) {
                initialize()
            }
            return ApiProviderManager.providerDataCollection
        }

    override val repositoryFile: RepositoryFileService by lazy {
        CachedRepositoryFile(
            LocalRepositoryFileService(ideaProject = project),
            MemoryCache()
        )
    }

    init {
        dispatcher.addListener(myProjectEventListener)
    }

    protected fun bindDataProviderForNotifiers() {
        val connection = messageBus.connect(project)
        connection.subscribe(MergeRequestDataNotifier.TOPIC, MergeRequestDataProvider(
            getApplicationService(),
            project,
            messageBus
        ))
    }

    override fun findFiltersByProviderId(id: String): Pair<GetMergeRequestFilter, MergeRequestOrdering> {
        val data = myFiltersData[id]
        return if (null !== data && getApplicationService().settings.saveMRFilterState) {
            data
        } else {
            Pair(
                GetMergeRequestFilterImpl(
                    state = MergeRequestState.OPENED,
                    search = "",
                    authorId = "",
                    assigneeId = "",
                    approverIds = listOf("")
                ),
                MergeRequestOrdering.RECENTLY_UPDATED
            )
        }
    }

    override fun saveFiltersOfProvider(id: String, filters: GetMergeRequestFilter, ordering: MergeRequestOrdering) {
        if (getApplicationService().settings.saveMRFilterState) {
            myFiltersData[id] = Pair(filters, ordering)
        }
    }

    override fun readStateItem(item: Element, id: String, settings: ProviderSettings) {
        super.readStateItem(item, id, settings)
        val attribute = item.getAttribute("savedFilters")
        if (null !== attribute) {
            val data = SavedFiltersUtil.parse(attribute.value)
            if (null !== data) {
                myFiltersData[id] = data
            }
        }
    }

    override fun writeStateItem(item: Element, id: String, settings: ProviderSettings) {
        super.writeStateItem(item, id, settings)
        val data = myFiltersData[id]
        if (null !== data) {
            item.setAttribute("savedFilters", SavedFiltersUtil.stringify(data.first, data.second))
        }
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
        if (settings.info.id == Github.id) {
            name = GithubConnectionsConfigurableBase.findNameFromId(settings.id)
        }

        val task = RegisterProviderTask(
            applicationService = getApplicationService(),
            ideaProject = project,
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

    override fun findReviewContextWhichDoingCodeReview(): ReviewContext? {
        return if (this.isDoingCodeReview()) {
            return ReviewContextManager.findSelectedContext()
        } else null
    }

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
        dispatcher.multicaster.codeReviewCommentsSet(providerData, mergeRequest, comments)
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
        dispatcher.multicaster.codeReviewCommitsSet(providerData, mergeRequest, commits)
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
        notification.notify(project)
    }
}