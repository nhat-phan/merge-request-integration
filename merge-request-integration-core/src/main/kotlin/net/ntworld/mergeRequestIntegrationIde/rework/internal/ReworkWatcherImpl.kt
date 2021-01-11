package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.openapi.vcs.changes.PreviewDiffVirtualFile
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequest.request.CreateCommentRequest
import net.ntworld.mergeRequest.request.PublishCommentsRequest
import net.ntworld.mergeRequest.request.ReplyCommentRequest
import net.ntworld.mergeRequest.request.UpdateCommentRequest
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.debug
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.DiffPreviewProviderImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ReviewContextImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.DiffNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ReworkEditorNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ReworkWatcherNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.*
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import net.ntworld.mergeRequestIntegrationIde.task.FindMergeRequestTask
import net.ntworld.mergeRequestIntegrationIde.task.GetCommentsTask
import net.ntworld.mergeRequestIntegrationIde.task.GetCommitsTask
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import java.util.*

class ReworkWatcherImpl(
    override val projectServiceProvider: ProjectServiceProvider,
    override val repository: GitRepository,
    override val branchName: String,
    override val providerData: ProviderData,
    override val mergeRequestInfo: MergeRequestInfo
) : ReworkWatcher, ReworkWatcherNotifier {
    private val myConnection = projectServiceProvider.messageBus.connect()
    private val myEditorManager = projectServiceProvider.messageBus.syncPublisher(
        ReworkEditorNotifier.TOPIC
    )
    private val myDiffPublisher = projectServiceProvider.messageBus.syncPublisher(DiffNotifier.TOPIC)

    private val myPreviewDiffVirtualFileMap = mutableMapOf<Change, PreviewDiffVirtualFile>()
    private val myCommentsMap = Collections.synchronizedMap(mutableMapOf<String, List<Comment>>())
    private val myChangesMap = Collections.synchronizedMap(mutableMapOf<String, MutableList<Change>>())
    private val myComments = mutableListOf<Comment>()
    private val myReworkGeneralCommentsView = ReworkGeneralCommentsView(projectServiceProvider, providerData, this)

    @Volatile
    private var myTerminate = false
    private var myIsChangesBuilt = false
    private var myIsFetchedComments = false
    private var myRunCount = 0
    private var myMergeRequest: MergeRequest? = null
    override var commits: List<Commit> = listOf()
    override var changes: List<Change> = listOf()
    override var comments: MutableList<Comment> = mutableListOf()
        private set

    override var onlyShowDraftComments: Boolean = false
        private set
    override var displayResolvedComments: Boolean = false
        private set

    override val interval: Long = 10000
    private val myGetCommitsTaskListener = object : GetCommitsTask.Listener {
        override fun dataReceived(mergeRequest: MergeRequest, commits: List<Commit>) {
            this@ReworkWatcherImpl.commits = commits
            changes = projectServiceProvider.repositoryFile.findChanges(providerData, mergeRequest, commits.map { it.id })
            buildChangesMap()
            myIsChangesBuilt = true
            ApplicationManager.getApplication().invokeLater {
                projectServiceProvider.openSingleMRToolWindow {
                    projectServiceProvider.singleMRToolWindowNotifierTopic.showReworkChanges(
                        this@ReworkWatcherImpl, changes
                    )
                }

                myEditorManager.bootstrap(this@ReworkWatcherImpl)
            }
        }
    }
    private val myGetCommentsTaskListener = object : GetCommentsTask.Listener {
        override fun dataReceived(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            comments: List<Comment>
        ) {
            myComments.clear()
            myComments.addAll(comments)
            buildCommentsAndSendCommentsUpdatedSignal()
            myIsFetchedComments = true
        }
    }
    private val myFindMergeRequestTaskListener = object : FindMergeRequestTask.Listener {
        override fun dataReceived(mergeRequest: MergeRequest) {
            myMergeRequest = mergeRequest

            fetchCommits(mergeRequest)
        }
    }

    private val reviewContext = ReviewContextImpl(
        projectServiceProvider, providerData, mergeRequestInfo, projectServiceProvider.messageBus.connect()
    )

    init {
        myConnection.subscribe(ReworkWatcherNotifier.TOPIC, this)
        projectServiceProvider.singleMRToolWindowNotifierTopic.registerReworkWatcher(this)
        fetchMergeRequest()
    }

    override fun isChangesBuilt(): Boolean {
        return myIsChangesBuilt
    }

    override fun isFetchedComments(): Boolean {
        return myIsFetchedComments
    }

    override fun canExecute(): Boolean {
        return !myTerminate && !projectServiceProvider.project.isDisposed
    }

    override fun shouldTerminate(): Boolean {
        return myTerminate ||
            projectServiceProvider.project.isDisposed ||
            repository.currentBranchName != branchName ||
            projectServiceProvider.isDoingCodeReview() ||
            !projectServiceProvider.applicationSettings.enableReworkProcess
    }

    override fun execute() {
        // the watcher runs every 10 seconds, so 18x10s = 3 minutes
        if (myRunCount % 18 == 0) {
            fetchComments()
        }
        myRunCount += 1
    }

    override fun terminate() {
        debug("${providerData.id}:$branchName: ReworkWatcher is terminated")
        if (!projectServiceProvider.messageBus.isDisposed) {
            projectServiceProvider.singleMRToolWindowNotifierTopic.removeReworkWatcher(this)
        }
        projectServiceProvider.reworkManager.markReworkWatcherTerminated(this)
        myConnection.disconnect()
    }

    override fun shutdown() {
        debug("${providerData.id}:$branchName: terminate ReworkWatcher")
        if (!projectServiceProvider.messageBus.isDisposed) {
            projectServiceProvider.singleMRToolWindowNotifierTopic.removeReworkWatcher(this)
        }
        ApplicationManager.getApplication().invokeLater {
            val fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(projectServiceProvider.project)
            myPreviewDiffVirtualFileMap.forEach { (_, diffFile) ->
                fileEditorManagerEx.closeFile(diffFile)
            }
            myPreviewDiffVirtualFileMap.clear()

            myEditorManager.shutdown(providerData)
        }
        myTerminate = true
    }

    override fun openChange(change: Change) {
        val afterRevision = change.afterRevision
        if (null === afterRevision) {
            updateDataToReviewContext()
            return openChangeAsDiffView(change)
        }
        myEditorManager.open(providerData, afterRevision.file.path)
    }

    override fun findChangeByPath(absolutePath: String): Change? {
        return if (myTerminate) null else {
            val changes = myChangesMap[absolutePath]
            if (null === changes) {
                return null
            }
            return changes.first()
        }
    }

    override fun findCommentsByPath(absolutePath: String): List<Comment> {
        return myCommentsMap[absolutePath] ?: listOf()
    }

    override fun fetchComments() {
        debug("${providerData.id}:$branchName: fetching comments")
        val task = GetCommentsTask(
            projectServiceProvider = projectServiceProvider,
            providerData = providerData,
            mergeRequestInfo = mergeRequestInfo,
            listener = myGetCommentsTaskListener
        )
        task.start()
    }

    override fun requestFetchComment(providerData: ProviderData) = assertHasSameProvider(providerData) {
        fetchComments()
    }

    override fun changeDisplayResolvedComments(
        providerData: ProviderData, value: Boolean
    ) = assertHasSameProvider(providerData) {
        if (value != displayResolvedComments) {
            displayResolvedComments = value
            buildCommentsAndSendCommentsUpdatedSignal()
        }
    }

    override fun changeOnlyShowDraftComments(providerData: ProviderData, value: Boolean) = assertHasSameProvider(providerData) {
        if (value != onlyShowDraftComments) {
            onlyShowDraftComments = value
            buildCommentsAndSendCommentsUpdatedSignal()
        }
    }

    override fun commentTreeNodeSelected(
        providerData: ProviderData,
        node: Node,
        type: CommentTreeView.TreeSelectType
    ) = assertHasSameProvider(providerData) {
        if (type == CommentTreeView.TreeSelectType.NORMAL) {
            return@assertHasSameProvider
        }
        when (node) {
            is GeneralCommentsNode -> displayGeneralComments(node.groupComments(), false)
            is ThreadNode -> when (val parent = node.parent) {
                is GeneralCommentsNode -> displayGeneralComments(parent.groupComments(), false)
                is FileLineNode -> openFileAndDisplayThreadByLineNode(parent)
            }
            is CommentNode -> when (val parent = node.parent!!.parent) {
                is GeneralCommentsNode -> displayGeneralComments(parent.groupComments(), false)
                is FileLineNode -> openFileAndDisplayThreadByLineNode(parent)
            }
            is FileNode -> openFileByNode(node)
            is FileLineNode -> openFileAndDisplayThreadByLineNode(node)
        }
    }

    override fun openCreateGeneralCommentForm(providerData: ProviderData) = assertHasSameProvider(providerData) {
        val groupedComments = mutableMapOf<String, MutableList<Comment>>()
        comments.forEach {
            if (null !== it.position) {
                return@forEach
            }
            if (!groupedComments.containsKey(it.parentId)) {
                groupedComments[it.parentId] = mutableListOf()
            }
            groupedComments[it.parentId]!!.add(it)
        }
        displayGeneralComments(groupedComments, true)
    }

    override fun requestReplyComment(
        providerData: ProviderData,
        content: String,
        repliedComment: Comment
    ) = assertHasSameProvider(providerData) {
        projectServiceProvider.infrastructure.serviceBus() process ReplyCommentRequest.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            repliedComment = repliedComment,
            body = content
        ) ifError {
            projectServiceProvider.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        fetchComments()
    }

    override fun requestCreateComment(
        providerData: ProviderData,
        content: String,
        position: GutterPosition?,
        isDraft: Boolean
    ) = assertHasSameProvider(providerData) {
        val commentPosition = convertGutterPositionToCommentPosition(position)
        projectServiceProvider.infrastructure.serviceBus() process CreateCommentRequest.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            position = commentPosition,
            body = content,
            isDraft = false
        ) ifError {
            projectServiceProvider.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        fetchComments()
    }

    override fun requestEditComment(providerData: ProviderData, comment: Comment, content: String) {
        projectServiceProvider.infrastructure.serviceBus() process UpdateCommentRequest.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            comment = comment,
            body = content
        ) ifError {
            projectServiceProvider.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        fetchComments()
    }

    override fun requestPublishComment(providerData: ProviderData, comment: Comment) {
        projectServiceProvider.infrastructure.serviceBus() process PublishCommentsRequest.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            draftCommentIds = listOf(comment.id)
        ) ifError {
            projectServiceProvider.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        fetchComments()
    }

    override fun requestDeleteComment(
        providerData: ProviderData,
        comment: Comment
    ) = assertHasSameProvider(providerData) {
        projectServiceProvider.infrastructure.commandBus() process DeleteCommentCommand.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            comment = comment
        )
        fetchComments()
    }

    override fun requestResolveComment(
        providerData: ProviderData,
        comment: Comment
    ) = assertHasSameProvider(providerData) {
        projectServiceProvider.infrastructure.commandBus() process ResolveCommentCommand.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            comment = comment
        )
        fetchComments()
    }

    override fun requestUnresolveComment(
        providerData: ProviderData,
        comment: Comment
    ) = assertHasSameProvider(providerData) {
        projectServiceProvider.infrastructure.commandBus() process UnresolveCommentCommand.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequestInfo.id,
            comment = comment
        )
        fetchComments()
    }

    private fun convertGutterPositionToCommentPosition(input: GutterPosition?): CommentPosition? {
        if (null === input) return null

        return CommentPositionImpl(
            oldLine = input.oldLine,
            oldPath = if (null === input.oldPath) null else RepositoryUtil.findRelativePath(repository, input.oldPath),
            newLine = input.newLine,
            newPath = if (null === input.newPath) null else RepositoryUtil.findRelativePath(repository, input.newPath),
            baseHash = if (input.baseHash.isNullOrEmpty()) findBaseHash() else input.baseHash,
            headHash = if (input.headHash.isNullOrEmpty()) findHeadHash() else input.headHash,
            startHash = if (input.startHash.isNullOrEmpty()) findStartHash() else input.startHash,
            source = CommentPositionSource.UNKNOWN,
            changeType = CommentPositionChangeType.UNKNOWN
        )
    }

    private fun findBaseHash(): String {
        if (commits.isNotEmpty()) {
            return commits.last().id
        }

        val mr = myMergeRequest ?: return ""
        val diff = mr.diffReference
        return if (null === diff) "" else diff.baseHash
    }

    private fun findStartHash(): String {
        val mr = myMergeRequest ?: return ""
        val diff = mr.diffReference
        return if (null === diff) "" else diff.startHash
    }

    private fun findHeadHash(): String {
        if (commits.isNotEmpty()) {
            return commits.first().id
        }

        val mr = myMergeRequest ?: return ""
        val diff = mr.diffReference
        return if (null === diff) "" else diff.headHash
    }

    private fun openFileByNode(node: FileNode) {
        val fullPath = RepositoryUtil.findAbsoluteCrossPlatformsPath(repository, node.path)
        openFileAndDisplayThread(absolutePath = fullPath, line = null, position = null)
    }

    private fun openFileAndDisplayThreadByLineNode(node: FileLineNode) {
        val fullPath = RepositoryUtil.findAbsoluteCrossPlatformsPath(repository, node.path)
        if (null === node.position.newLine) {
            // edge case, open diff view instead of editor view if there is no newLine
            val change = findChangeByPath(fullPath)
            if (null !== change) {
                updateDataToReviewContext()
                openChangeAsDiffView(change)
                reviewContext.putChangeData(change, DiffNotifier.ScrollPosition, node.position)
                reviewContext.putChangeData(change, DiffNotifier.ScrollShowComments, true)
                myDiffPublisher.hideAllCommentsRequested(reviewContext, change)
                myDiffPublisher.scrollToPositionRequested(reviewContext, change, node.position, true)
            }
        } else {
            openFileAndDisplayThread(absolutePath = fullPath, line = node.line, position = node.position)
        }
    }

    private fun openFileAndDisplayThread(absolutePath: String, line: Int?, position: CommentPosition?) {
        val change = findChangeByPath(absolutePath)
        if (null === change) {
            return myEditorManager.open(providerData, absolutePath, line)
        }

        val afterRevision = change.afterRevision
        if (null === afterRevision) {
            updateDataToReviewContext()
            openChangeAsDiffView(change)
            if (null !== position) {
                reviewContext.putChangeData(change, DiffNotifier.ScrollPosition, position)
                reviewContext.putChangeData(change, DiffNotifier.ScrollShowComments, true)
                myDiffPublisher.hideAllCommentsRequested(reviewContext, change)
                myDiffPublisher.scrollToPositionRequested(reviewContext, change, position, true)
            }
            return
        }
        return myEditorManager.open(providerData, absolutePath, line)
    }

    private fun displayGeneralComments(groupedComments: Map<String, List<Comment>>, focusToEditor: Boolean) {
        myReworkGeneralCommentsView.render(groupedComments)
        if (focusToEditor) {
            myReworkGeneralCommentsView.focusMainEditor()
        }
    }

    private fun buildCommentsMap() {
        myCommentsMap.clear()
        val grouped = CommentUtil.groupCommentsByNewPath(comments)
        grouped.forEach { (relativePath, list) ->
            val fullPath = RepositoryUtil.findAbsoluteCrossPlatformsPath(repository, relativePath)
            myCommentsMap[fullPath] = list
        }
    }

    private fun buildChangesMap() {
        myChangesMap.clear()
        for (change in changes) {
            val filePaths = ChangesUtil.getPathsCaseSensitive(change)
            for (filePath in filePaths) {
                val path = filePath.path
                val list = myChangesMap.get(path)
                if (null === list) {
                    myChangesMap[path] = mutableListOf(change)
                } else {
                    if (!list.contains(change)) {
                        list.add(change)
                    }
                }
            }
        }
    }

    private fun openChangeAsDiffView(change: Change) {
        val project = projectServiceProvider.project
        val diffFile = myPreviewDiffVirtualFileMap[change]
        if (null === diffFile) {
            val provider = DiffPreviewProviderImpl(project, change, reviewContext, false)
            val created = PreviewDiffVirtualFile(provider)
            myPreviewDiffVirtualFileMap[change] = created
            FileEditorManagerEx.getInstanceEx(project).openFile(created, true)
        } else {
            FileEditorManagerEx.getInstanceEx(project).openFile(diffFile, true)
        }
    }

    private fun updateDataToReviewContext() {
        val mr = myMergeRequest
        if (null !== mr) {
            reviewContext.diffReference = mr.diffReference
        }
        reviewContext.commits = commits
        reviewContext.reviewingCommits = commits
        reviewContext.changes = changes
        reviewContext.reviewingChanges = changes
        reviewContext.comments = comments
    }

    private fun fetchCommits(mergeRequest: MergeRequest) {
        debug("${providerData.id}:$branchName: fetching commits")
        val task = GetCommitsTask(
            projectServiceProvider = projectServiceProvider,
            providerData = providerData,
            mergeRequest = mergeRequest,
            listener = myGetCommitsTaskListener
        )
        task.start()
    }

    private fun fetchMergeRequest() {
        debug("${providerData.id}:$branchName: fetching mergeRequest")
        val task = FindMergeRequestTask(
            projectServiceProvider = projectServiceProvider,
            providerData = providerData,
            mergeRequestInfo = mergeRequestInfo,
            listener = myFindMergeRequestTaskListener
        )
        task.start()
    }

    private fun assertHasSameProvider(providerData: ProviderData, invoker: (() -> Unit)) {
        if (this.providerData.id == providerData.id) {
            invoker()
        }
    }

    private fun buildCommentsAndSendCommentsUpdatedSignal() {
        comments.clear()
        if (onlyShowDraftComments) {
            comments.addAll(myComments.filter { it.isDraft })
        } else {
            if (displayResolvedComments) {
                comments.addAll(myComments)
            } else {
                comments.addAll(myComments.filter { !it.resolved })
            }
        }
        buildCommentsMap()

        myEditorManager.commentsUpdated(providerData)
        ApplicationManager.getApplication().invokeLater {
            projectServiceProvider.singleMRToolWindowNotifierTopic.showReworkComments(
                this, comments, displayResolvedComments
            )
        }
    }
}