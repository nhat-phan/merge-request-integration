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
import com.intellij.openapi.util.text.StringUtil
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
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
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
    override val repository: GitRepository? = RepositoryUtil.findRepository(ideaProject, providerData)

    private var myComments: Collection<Comment> = listOf()
    private var myChanges: Collection<Change> = listOf()
    private var myCommits: Collection<Commit> = listOf()

    override var commits: Collection<Commit>
        get() = myCommits
        set(value) {
            myCommits = value
        }

    override var changes: Collection<Change>
        get() = myChanges
        set(value) {
            myChanges = value
            buildChangesMap(value)
        }

    override var comments: Collection<Comment>
        get() = myComments
        set(value) {
            myComments = value
            buildCommentsMap(value)
        }


    private val myCommentsMap = mutableMapOf<String, MutableList<Comment>>()
    private val myChangesMap = mutableMapOf<String, MutableList<Change>>()

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

    private fun buildCommentsMap(value: Collection<Comment>) {
        if (null === repository) {
            return
        }
        myCommentsMap.clear()
        for (comment in value) {
            val position = comment.position
            if (null === position) {
                continue
            }
            if (null !== position.newPath) {
                doHashComment(repository, position.newPath!!, comment)
            }
            if (null !== position.oldPath) {
                doHashComment(repository, position.oldPath!!, comment)
            }
        }
    }

    private fun doHashComment(repository: GitRepository, path: String, comment: Comment) {
        val fullPath = RepositoryUtil.findAbsolutePath(repository, path)
        val list = myCommentsMap[fullPath]
        if (null === list) {
            myCommentsMap[fullPath] = mutableListOf(comment)
        } else {
            if (!list.contains(comment)) {
                list.add(comment)
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
                return findCommentPositionForDeletedCase(
                    info.change.beforeRevision!!,
                    editor.caretModel.logicalPosition
                )
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
            oldPath = RepositoryUtil.findRelativePath(repository, beforeRevision.file.path),
            newPath = RepositoryUtil.findRelativePath(repository, afterRevision.file.path),
            oldLine = (gutterMyLineNumberConvertor.get(gutter) as TIntFunction).execute(logicalPosition.line + 1),
            newLine = (gutterMyAdditionalLineNumberConvertor.get(gutter) as TIntFunction).execute(logicalPosition.line + 1),
            source = CommentPositionSource.UNIFIED
        )
    }

    override fun findChangeInfoByPathAndContent(path: String, content: String): CodeReviewManager.ChangeInfo? {
        val changes = myChangesMap[path]
        if (null === changes || changes.isEmpty()) {
            return null
        }

        for (change in changes) {
            val beforeRevision = change.beforeRevision
            if (null !== beforeRevision && compareContent(beforeRevision.content, content)) {
                return ChangeInfoImpl(
                    change = change,
                    contentRevision = beforeRevision,
                    before = true,
                    after = false
                )
            }
            val afterRevision = change.afterRevision
            if (null !== afterRevision && compareContent(afterRevision.content, content)) {
                return ChangeInfoImpl(
                    change = change,
                    contentRevision = afterRevision,
                    before = false,
                    after = true
                )
            }
        }
        return null
    }

    private fun compareContent(left: String?, right: String?): Boolean {
        // we cannot compare content directly because newline in Windows & macOS is different
        // newline in Windows is \r\n but in macOs or Linux it is \n
        // so we compare each line of the content
        if (null !== left && null !== right) {
            return StringUtil.convertLineSeparators(left).equals(
                StringUtil.convertLineSeparators(right)
            )
        }
        return left.equals(right)
    }

    override fun findCommentPoints(path: String, changeInfo: CodeReviewManager.ChangeInfo): List<CommentPoint> {
        val comments = myCommentsMap[path]
        if (null === comments || comments.isEmpty()) {
            return listOf()
        }

        val result = mutableListOf<CommentPoint>()
        for (comment in comments) {
            val position = comment.position!!
            if (position.headHash == changeInfo.contentRevision.revisionNumber.asString()) {
                if (changeInfo.before && null !== position.oldLine) {
                    result.add(CommentPoint(position.oldLine!!, comment))
                    continue
                }
                if (changeInfo.after && null !== position.newLine) {
                    result.add(CommentPoint(position.newLine!!, comment))
                    continue
                }
            }
        }
        return result
    }

    private fun findCommentPositionForSideBySideViewer(
        info: CodeReviewManager.ChangeInfo,
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
            newPath = RepositoryUtil.findRelativePath(repository, afterRevision.file.path),
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
            oldPath = RepositoryUtil.findRelativePath(repository, beforeRevision.file.path),
            newPath = null,
            oldLine = logicalPosition.line + 1,
            newLine = -1,
            source = CommentPositionSource.SIDE_BY_SIDE_LEFT
        )
    }

    private fun findCommentPositionForSideBySideViewerModifiedCase(
        info: CodeReviewManager.ChangeInfo,
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

        val document1 = DocumentImpl(beforeRevision.content ?: "", true, false)
        val document2 = DocumentImpl(afterRevision.content ?: "", true, false)
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
                oldPath = RepositoryUtil.findRelativePath(repository, beforeRevision.file.path),
                newPath = RepositoryUtil.findRelativePath(repository, afterRevision.file.path),
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
                oldPath = RepositoryUtil.findRelativePath(repository, beforeRevision.file.path),
                newPath = RepositoryUtil.findRelativePath(repository, afterRevision.file.path),
                oldLine = oldLine,
                newLine = newLine,
                source = CommentPositionSource.SIDE_BY_SIDE_LEFT
            )
        }

        return null
    }

    private fun findBaseHash(): String {
        if (commits.isNotEmpty()) {
            return commits.last().id
        }

        val diff = mergeRequest.diffReference
        return if (null === diff) "" else diff.baseHash
    }

    private fun findStartHash(): String {
        val diff = mergeRequest.diffReference
        return if (null === diff) "" else diff.startHash
    }

    private fun findHeadHash(): String {
        if (commits.isNotEmpty()) {
            return commits.first().id
        }

        val diff = mergeRequest.diffReference
        return if (null === diff) "" else diff.headHash
    }

    private data class ChangeInfoImpl(
        override val change: Change,
        override val contentRevision: ContentRevision,
        override val before: Boolean,
        override val after: Boolean
    ) : CodeReviewManager.ChangeInfo

}