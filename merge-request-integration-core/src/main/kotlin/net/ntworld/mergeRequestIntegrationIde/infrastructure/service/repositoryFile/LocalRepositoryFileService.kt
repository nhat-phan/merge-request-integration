package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.vcs.log.impl.VcsLogContentUtil
import com.intellij.vcs.log.util.VcsLogUtil
import net.ntworld.mergeRequest.ProviderData
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import javax.swing.Icon

class LocalRepositoryFileService(
    private val ideaProject: IdeaProject
) : RepositoryFileService {
    private val myLogger = Logger.getInstance(this.javaClass)

    override fun findChanges(providerData: ProviderData, hashes: List<String>): List<Change> {
        try {
            val repository = RepositoryUtil.findRepository(ideaProject, providerData)
            if (null === repository) {
                return listOf()
            }
            val log = VcsLogContentUtil.getOrCreateLog(ideaProject)
            if (null === log) {
                return listOf()
            }

            val details = VcsLogUtil.getDetails(
                log.dataManager.getLogProvider(repository.root),
                repository.root,
                hashes
            )
            return VcsLogUtil.collectChanges(details) { it.changes }
        } catch (exception: Exception) {
            myLogger.info("Cannot findChanges for ${providerData.repository}, hashes: ${hashes.joinToString(",")}")
            throw exception
        }
    }

    override fun findIcon(providerData: ProviderData, path: String): Icon {
        val repository = RepositoryUtil.findRepository(ideaProject, providerData)
        val psiFile = findPsiFile(RepositoryUtil.findAbsoluteCrossPlatformsPath(repository, path))
        if (null === psiFile) {
            return AllIcons.FileTypes.Any_type
        }

        return try {
            psiFile.getIcon(Iconable.ICON_FLAG_READ_STATUS)
        } catch (exception: Exception) {
            AllIcons.FileTypes.Any_type
        }
    }

    private fun findPsiFile(path: String): PsiFile? {
        val file = findVirtualFileByPath(path)
        if (null === file) {
            return null
        }
        return PsiManager.getInstance(ideaProject).findFile(file)
    }

    fun findVirtualFileByPath(path: String): VirtualFile? {
        val file = LocalFileSystem.getInstance().findFileByPath(path)
        if (null !== file) {
            return file
        }
        return null
    }
}