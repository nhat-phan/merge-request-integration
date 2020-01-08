package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.tools.fragmented.UnifiedFragmentBuilder
import com.intellij.diff.util.DiffUtil
import com.intellij.diff.util.Side
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.PreviewDiffVirtualFile
import git4idea.repo.GitRepository
import gnu.trove.TIntFunction
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil
import net.ntworld.mergeRequestIntegrationIde.ui.editor.EditorUtil
import net.ntworld.mergeRequestIntegrationIde.ui.service.DisplayChangesService
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import java.lang.Exception

internal class CodeReviewManagerImpl(
    private val ideaProject: IdeaProject,
    override val providerData: ProviderData,
    override val mergeRequest: MergeRequest,
    val util: CodeReviewUtil
) : CodeReviewManager, CodeReviewUtil by util {
    private var myChanges: Collection<Change> = listOf()

    override var commits: Collection<Commit> = listOf()

    override var changes: Collection<Change>
        get() = myChanges
        set(value) {
            myChanges = value
            buildChangesMap(value)
        }

    override var comments: Collection<Comment> = listOf()

    private val myChangesMap = mutableMapOf<String, MutableList<Change>>()
    private val myRepository: GitRepository? = RepositoryUtil.findRepository(ideaProject, providerData)

    private fun buildChangesMap(value: Collection<Change>) {
        myChangesMap.clear()
        for (change in value) {
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

    override fun findCommentPosition(editor: Editor, caret: Caret?, dataContext: DataContext?): CommentPosition? {
        if (editor !is EditorEx) {
            return null
        }
        val path = EditorUtil.findVirtualFilePath(editor)

        if (null === path) {
            return findCommentPositionForUnifiedView(editor, caret, dataContext)
        }

        val info = findChangeInfoByPathAndContent(path, editor.document.text)
        if (null === info) {
            return null
        }

        try {
            return findCommentPositionForSideBySideViewer(info, editor.caretModel.logicalPosition)
        } catch (exception: Exception) {
            if (info.after && null !== info.change.afterRevision) {
                return findCommentPositionForAddedCase(info.change.afterRevision!!, editor.caretModel.logicalPosition)
            }
            if (info.before && null !== info.change.beforeRevision) {
                return findCommentPositionForDeletedCase(info.change.beforeRevision!!, editor.caretModel.logicalPosition)
            }
            return null
        }
    }

    private fun findCommentPositionForUnifiedView(
        editor: Editor,
        caret: Caret?,
        dataContext: DataContext?
    ): CommentPosition? {
        if (null == dataContext) {
            return null
        }
        val virtualFile = dataContext.getData("virtualFile")
        if (null === virtualFile || virtualFile !is PreviewDiffVirtualFile) {
            return null
        }
        val provider = virtualFile.provider
        if (provider !is DisplayChangesService.MyDiffPreviewProvider) {
            return null
        }

        val change = provider.change
        if (!changes.contains(change)) {
            return null
        }

        val logicalPosition = editor.caretModel.logicalPosition
        val beforeRevision = change.beforeRevision
        val afterRevision = change.afterRevision
        if (null === beforeRevision && null === afterRevision) {
            return null
        }

        if (null === beforeRevision && null !== afterRevision) {
            return findCommentPositionForAddedCase(afterRevision, logicalPosition)
        }

        if (null !== beforeRevision && null === afterRevision) {
            return findCommentPositionForDeletedCase(beforeRevision, logicalPosition)
        }

        val gutter = editor.gutter
        val gutterMyLineNumberConvertor = gutter.javaClass.getDeclaredField("myLineNumberConvertor")
        val gutterMyAdditionalLineNumberConvertor = gutter.javaClass.getDeclaredField("myAdditionalLineNumberConvertor")
        gutterMyLineNumberConvertor.isAccessible = true
        gutterMyAdditionalLineNumberConvertor.isAccessible = true
        return CommentPositionImpl(
            baseHash = beforeRevision!!.revisionNumber.asString(),
            startHash = findStartHash(),
            headHash = afterRevision!!.revisionNumber.asString(),
            oldPath = RepositoryUtil.findRelativePath(myRepository, beforeRevision.file.path),
            newPath = RepositoryUtil.findRelativePath(myRepository, afterRevision.file.path),
            oldLine = (gutterMyLineNumberConvertor.get(gutter) as TIntFunction).execute(logicalPosition.line + 1),
            newLine = (gutterMyAdditionalLineNumberConvertor.get(gutter) as TIntFunction).execute(logicalPosition.line + 1),
            source = CommentPositionSource.UNIFIED
        )
    }

    private fun findChangeInfoByPathAndContent(path: String, content: String): ChangeInfo? {
        val changes = myChangesMap[path]
        if (null === changes || changes.isEmpty()) {
            return null
        }

        for (change in changes) {
            val beforeRevision = change.beforeRevision
            if (null !== beforeRevision && beforeRevision.content == content) {
                return ChangeInfo(
                    change = change,
                    contentRevision = beforeRevision,
                    before = true,
                    after = false
                )
            }
            val afterRevision = change.afterRevision
            if (null !== afterRevision && afterRevision.content == content) {
                return ChangeInfo(
                    change = change,
                    contentRevision = afterRevision,
                    before = false,
                    after = true
                )
            }
        }
        return null
    }

    private fun findCommentPositionForSideBySideViewer(
        info: ChangeInfo,
        logicalPosition: LogicalPosition
    ): CommentPosition? {
        val beforeRevision = info.change.beforeRevision
        val afterRevision = info.change.afterRevision
        if (null === beforeRevision && null === afterRevision) {
            return null
        }

        if (null === beforeRevision && null !== afterRevision) {
            return findCommentPositionForAddedCase(afterRevision, logicalPosition)
        }

        if (null !== beforeRevision && null === afterRevision) {
            return findCommentPositionForDeletedCase(beforeRevision, logicalPosition)
        }
        return findCommentPositionForSideBySideViewerModifiedCase(
            info, beforeRevision!!, afterRevision!!, logicalPosition
        )
    }

    private fun findCommentPositionForAddedCase(
        afterRevision: ContentRevision,
        logicalPosition: LogicalPosition
    ): CommentPosition? {
        return CommentPositionImpl(
            baseHash = findBaseHash(),
            startHash = findStartHash(),
            headHash = afterRevision.revisionNumber.asString(),
            oldPath = null,
            newPath = RepositoryUtil.findRelativePath(myRepository, afterRevision.file.path),
            oldLine = null,
            newLine = logicalPosition.line + 1,
            source = CommentPositionSource.SIDE_BY_SIDE_RIGHT
        )
    }

    private fun findCommentPositionForDeletedCase(
        beforeRevision: ContentRevision,
        logicalPosition: LogicalPosition
    ): CommentPosition? {
        return CommentPositionImpl(
            baseHash = beforeRevision.revisionNumber.asString(),
            startHash = findStartHash(),
            headHash = findHeadHash(),
            oldPath = RepositoryUtil.findRelativePath(myRepository, beforeRevision.file.path),
            newPath = null,
            oldLine = logicalPosition.line + 1,
            newLine = -1,
            source = CommentPositionSource.SIDE_BY_SIDE_LEFT
        )
    }

    private fun findCommentPositionForSideBySideViewerModifiedCase(
        info: ChangeInfo,
        beforeRevision: ContentRevision,
        afterRevision: ContentRevision,
        logicalPosition: LogicalPosition
    ): CommentPosition? {
        val manager = ComparisonManager.getInstance()
        val lineFragments = manager.compareLinesInner(
            afterRevision.content ?: "",
            beforeRevision.content ?: "",
            ComparisonPolicy.DEFAULT,
            DumbProgressIndicator.INSTANCE
        )

        val document1 = DocumentImpl(beforeRevision.content ?: "")
        val document2 = DocumentImpl(afterRevision.content ?: "")
        val contentConvertor1 = TIntFunction { it }
        val contentConvertor2 = TIntFunction { it }

        val unifiedFragmentBuilder = UnifiedFragmentBuilder(
            lineFragments, document1, document2, Side.RIGHT
        )
        unifiedFragmentBuilder.exec()
        val converter1: TIntFunction? = DiffUtil.mergeLineConverters(
            DiffUtil.mergeLineConverters(contentConvertor1, unifiedFragmentBuilder.convertor1.createConvertor()),
            TIntFunction { it }
        )
        val converter2: TIntFunction? = DiffUtil.mergeLineConverters(
            DiffUtil.mergeLineConverters(contentConvertor2, unifiedFragmentBuilder.convertor2.createConvertor()),
            TIntFunction { it }
        )

        if (info.after) {
            return CommentPositionImpl(
                baseHash = beforeRevision.revisionNumber.asString(),
                startHash = findStartHash(),
                headHash = afterRevision.revisionNumber.asString(),
                oldPath = RepositoryUtil.findRelativePath(myRepository, beforeRevision.file.path),
                newPath = RepositoryUtil.findRelativePath(myRepository, afterRevision.file.path),
                oldLine = converter2!!.execute(logicalPosition.line + 1),
                newLine = logicalPosition.line + 1,
                source = CommentPositionSource.SIDE_BY_SIDE_RIGHT
            )
        }

        if (info.before) {
            val oldLine = logicalPosition.line + 1
            val maxLine = document2.lineCount
            var newLine = -1
            for (i in 1..maxLine) {
                if (converter2!!.execute(i) == oldLine) {
                    newLine = i
                    break
                }
            }

            return CommentPositionImpl(
                baseHash = beforeRevision.revisionNumber.asString(),
                startHash = findStartHash(),
                headHash = afterRevision.revisionNumber.asString(),
                oldPath = RepositoryUtil.findRelativePath(myRepository, beforeRevision.file.path),
                newPath = RepositoryUtil.findRelativePath(myRepository, afterRevision.file.path),
                oldLine = oldLine,
                newLine = newLine,
                source = CommentPositionSource.SIDE_BY_SIDE_LEFT
            )
        }

        return null
    }

    private fun findBaseHash(): String {
        val diff = mergeRequest.diffReference
        return if (null === diff) "" else diff.baseHash
    }

    private fun findStartHash(): String {
        val diff = mergeRequest.diffReference
        return if (null === diff) "" else diff.startHash
    }

    private fun findHeadHash(): String {
        val diff = mergeRequest.diffReference
        return if (null === diff) "" else diff.headHash
    }

    private data class ChangeInfo(
        val change: Change,
        val contentRevision: ContentRevision,
        val before: Boolean,
        val after: Boolean
    )

}