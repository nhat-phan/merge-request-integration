package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequestIntegrationIde.SimplePresenter

interface DiffPresenter : SimplePresenter, Disposable {
    val model: DiffModel
    val view: DiffView<*>

    fun publishAllDraftComments()
}